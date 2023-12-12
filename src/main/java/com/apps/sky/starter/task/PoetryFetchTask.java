package com.apps.sky.starter.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.TimeInterval;
import com.apps.sky.starter.codec.AppDailyPoetryCodec;
import com.apps.sky.starter.domain.AppDailyPoetry;
import com.apps.sky.starter.utils.AppUtil;
import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Slf4j
public class PoetryFetchTask implements OriginRouter {


  private WebClient httpClient;

  private EventBus eventBus;

  //TODO：放配置文件中
  private static final String SENTENCE_URI = "http://localhost:8080/all";

  private static final String DALL_E_URI = "https://ai.ericsky.com/api/openai/v1/images/generations";

  private static final String TTS_PY = "/work/projects/sky_apps/tts.py";
  /**
   * 获取当天诗词配图失败的fallback地址
   */
  private static final String DEFAULT_IMG_URL = "https://skytools.cn/images/poetry/3.jpeg";

  //
  private static final String IMG_STORE_PATH = "/var/www/html/images/poetry/{date}/";
  private static final String AUDIO_STORE_PATH = "/var/www/html/audios/poetry/{date}/";
  //本地调试保存地址
  //private static final String IMG_STORE_PATH = "/Users/yang/data/{date}/";
  private static final String IMG_ACCESS_PATH = "https://skytools.cn/images/poetry/{date}/";

  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    //初始化http,redis,sql client
    initClient(originVertxContext, originConfig);
    //注册处理器
    registerEventBusConsumers(originVertxContext);

    //启动定时任务
    fetchDailyPoetryTask(originVertxContext);
    //定时调用生成poetry的语音文件
    poetryTTSTask(originVertxContext);

    Router router = originVertxContext.getRouter();
    //手动重新拉去诗句重新生成配图
    router.get("/api/re-fetch-poetry").handler(reFetchPoetryHandler());
    //将某天的诗词文字转语音
    router.get("/api/tts-poetry").handler(ttsPoetryHandler());

  }

  private Handler<RoutingContext> ttsPoetryHandler() {
    return ctx -> {
      String code = ctx.request().params().get("access_key");
      String date = ctx.request().params().get("date");
      if (code != null && code.equals(System.getenv("api_access_key"))) {
        poetryTTS(date);
        ctx.response().end("poetry tts for date:" + date);
      } else {
        ctx.fail(403);
      }
    };
  }

  private void poetryTTSTask(OriginVertxContext originVertxContext) {
    Vertx vertx = originVertxContext.getVertx();
    //设置00:12:00 生成音频
    vertx.setTimer(millisecondsToMidnight() + 12*60*1000, timerId -> {
      //vertx.setTimer(3000, timerId -> {//本地调试使用
      log.info("begin poetryTTSTask daily job at {}", LocalDateTimeUtil.now());
      poetryTTS(DateUtil.today());
      //每天取一次数据
      vertx.setPeriodic(24 * 60 * 60 * 1000, periodicId -> {
        log.info("begin poetryTTSTask at {}", LocalDateTimeUtil.now());
        poetryTTS(DateUtil.today());
      });
    });
  }

  private void poetryTTS(String date) {
    log.info("begin getPoetryByDay");
    String sql = "select '《' || title || '》 ' || dynasty || ', ' || author || '。 ' || array_to_string(origin_content, ' ') from app_daily_poetry where date = $1 order by id asc limit 1";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(date))
      .onComplete(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          if (rows.size() != 0) {
            Row row = rows.iterator().next();
            String poetry = row.getString(0);
            log.info("{}:{}" , date, poetry);
            generatePoetryMp3(date, poetry);
          } else {
            log.warn("data not found on date:" + date);
          }
        } else {
          log.error(("get daily poetry failed: " + ar.cause().getMessage()));
        }
        sqlClient.close();
      });
  }

  private void generatePoetryMp3(String date, String poetry) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("python3", TTS_PY,
        poetry, AUDIO_STORE_PATH.replace("{date}", date), "1.mp3");
      Process process = processBuilder.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        log.warn(line);
      }
      reader.close();
      //生成音频后，更新has_audio 字段
      updateHasAudio(date);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateHasAudio(String date) {
    String sql = "update app_daily_poetry set has_audio = $1 where date = $2 ";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(true, date))
      .onFailure(err -> {
        log.error("更新当天的has_audio失败，{}，{}", date, err.getMessage());
      }).onComplete(rs -> sqlClient.close());
  }

  private Handler<RoutingContext> reFetchPoetryHandler() {
    return ctx -> {
      String code = ctx.request().params().get("access_key");
      if (code != null && code.equals(System.getenv("api_access_key"))) {
        fetchDailyPoetry();
        ctx.response().end("begin re fetch poerty");
      } else {
        ctx.fail(403);
      }
    };
  }

  /**
   * 定时任务，每天00:00:10获取当天的诗词。
   * 流程：
   * 1. 从今日诗词获取诗句
   * 2. 使用DALL-E给核心诗句配图，并保存到Nginx服务器相关目录下提供访问。
   * 3. 保存相关数据到数据库持久化
   *
   * @param originVertxContext
   */
  private void fetchDailyPoetryTask(OriginVertxContext originVertxContext) {
    Vertx vertx = originVertxContext.getVertx();
    //设置00:00:10 开始取当天数据
    vertx.setTimer(millisecondsToMidnight() + 10000, timerId -> {
    //vertx.setTimer(3000, timerId -> {//本地调试使用
      log.info("begin to run daily job at {}", LocalDateTimeUtil.now());
      fetchDailyPoetry();
      //每天取一次数据
      vertx.setPeriodic(24 * 60 * 60 * 1000, periodicId -> {
        log.info("begin to fetch daily poetry at {}", LocalDateTimeUtil.now());
        fetchDailyPoetry();
      });
    });
  }


  private void fetchDailyPoetry() {
    //从环境变量中取今日诗词API的token，https://www.jinrishici.com/doc/#get-token
    String jrscToken = System.getenv("JRSC_TOKEN");

    httpClient.getAbs(SENTENCE_URI)
      .putHeader("X-User-Token", jrscToken)
      .send()
      .onSuccess(resp -> {
        JsonObject json = resp.bodyAsJsonObject();
        log.error("从今日诗词API获取诗词成功，{},{}", DateUtil.date(), json);
        AppDailyPoetry poetry = jsonToPoetry(json);

        //1. 发送到event bus，通知对应的consumer保存数据到数据库
        eventBus.send("fetch-daily-poetry-done", poetry);

        //2. 并行执行使用DELL-E生成配图。
        eventBus.send("generate-poetry-image", poetry.getContent());
      }).onFailure(err ->
        log.error("从今日诗词API获取诗词失败，{},{}", DateUtil.date(), err.getMessage())
      );
  }

  private void saveDailyPoetry(AppDailyPoetry poetry) {
    String sql = "INSERT INTO app_daily_poetry (date, content, title, dynasty, author, origin_content, img_list) " +
      "VALUES ($1, $2, $3, $4, $5, $6, $7)";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(
      poetry.getDate(),
      poetry.getContent(),
      poetry.getTitle(),
      poetry.getDynasty(),
      poetry.getAuthor(),
      poetry.getOriginContent().toArray(),
      poetry.getImgList().toArray()
    )).onSuccess(result -> {
      log.info("保存诗词信息到数据库成功，{}", DateUtil.today());
    }).onFailure(throwable -> {
      log.error("保存诗词信息到数据库失败，{}，{}", DateUtil.today(), throwable.getMessage());
    }).onComplete(rs -> sqlClient.close());
  }

  private void updateImageUrl(String revisedPrompt) {
    String today = DateUtil.today();
    String imgUrl = IMG_ACCESS_PATH.replace("{date}", today)+ "1.jpg";

    String sql = "update app_daily_poetry set img_list = $1, revised_prompt = $2 where date = $3 ";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(new String[] {imgUrl}, revisedPrompt, today))
      .onFailure(err -> {
        log.error("更新当天配图失败，{}，{}", today, err.getMessage());
      }).onComplete(rs -> sqlClient.close());
  }

  /**
   * 使用DELL-E3给核心诗句生成图片
   * 约定：保存到 /var/www/html/images/poetry/{date}/1.png
   * 会有个定时任务执行脚本compress_png_to_jpg.sh，把 1.png 压缩成 1.jpg
   * @param content 核心诗句
   */
  private void generateImages(String content) {
    TimeInterval timer = DateUtil.timer();

    //从环境变量中取OpenAI的API KEY
    String openAIAPIkey = System.getenv("OPENAI_API_KEY");
    String today = DateUtil.today();
    //JsonObject body = new JsonObject().put("model", "dall-e-2").put("prompt", "中国水墨画: " + content).put("n", 1).put("size", "512x512");
    JsonObject body = new JsonObject().put("model", "dall-e-3").put("prompt", content + " 中国水墨画风格").put("n", 1).put("size", "1024x1024");
    //TODO: 这个请求会比较慢
    httpClient.postAbs(DALL_E_URI).putHeader("Content-Type", "application/json")
      .bearerTokenAuthentication(openAIAPIkey)
      .sendJsonObject(body).onSuccess(resp -> {
        long costTime = timer.interval();
        JsonObject openAIJsonResp = resp.bodyAsJsonObject();
        log.info("使用OpenAI DELL-E生成配图耗时：{}， resp:{}", costTime, openAIJsonResp);
        //DALL-E3目前只返回一张图片
        JsonObject jsonObject = openAIJsonResp.getJsonArray("data")
          .getJsonObject(0);
        String url = jsonObject.getString("url");
        String revisedPrompt = jsonObject.getString("revised_prompt");

        String destinationPath = IMG_STORE_PATH.replace("{date}", today)+ "1.png";
        //TODO: 文件IO操作也比较慢
        try {
          AppUtil.saveImage(url, destinationPath);
          updateImageUrl(revisedPrompt);
        } catch (IOException e) {
          log.warn("保存诗词DALL-E3配图失败，使用默认配图。{}，error：{}", today, e.getMessage());
        }
      }).onFailure(err -> {
        log.warn("获取诗词DALL-E3配图失败，使用默认配图。{}，error：{}", today, err.getMessage());
      }).onComplete(ar -> {
        long costTime = timer.interval();
        log.info("使用OpenAI DELL-E生成配图并保存到文件系统总耗时：{}", costTime);
      });
  }

  private AppDailyPoetry jsonToPoetry(JsonObject data) {

    AppDailyPoetry poetry = new AppDailyPoetry();
    poetry.setDate(DateUtil.today());//今天
    poetry.setContent(data.getString("sentence"));
    poetry.setTitle(data.getString("title"));
    poetry.setDynasty(data.getString("dynasty"));
    poetry.setAuthor(data.getString("author"));
    poetry.setOriginContent(data.getJsonArray("paragraphs").getList());
    //先用默认图片, DALL-E3生成配图后再更新图片地址
    poetry.setImgList(Arrays.asList(DEFAULT_IMG_URL));
    return poetry;
  }

  private void registerEventBusConsumers(OriginVertxContext originVertxContext) {
    log.info("开始注册Event Bus Consumers...");
    //从今日诗词API获取到诗句后，保存当天的推荐诗词到数据库
    eventBus.consumer("fetch-daily-poetry-done", message -> {
      saveDailyPoetry((AppDailyPoetry) message.body());
    });
    //当天的推荐诗词到数据库后，使用DALL-E给核心诗句生成配图，并保存到Nginx 相关目录下
    eventBus.consumer("generate-poetry-image", message -> {
      WorkerExecutor executor = originVertxContext.getVertx().createSharedWorkerExecutor("http-and-io-operations");
      TimeInterval timer = DateUtil.timer();
      executor.executeBlocking(feature -> {
        generateImages((String) message.body());
      }, result -> {
        log.info("executeBlocking generateImages cost {} s", timer.interval()/1000);
      });

    });
  }

  private void initClient(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    log.info("初始化HttpClient");
    Vertx vertx = originVertxContext.getVertx();
    this.httpClient = WebClient.create(vertx);
    this.eventBus = originConfig.getEventBus();
    //注册编解码器
    eventBus.registerDefaultCodec(AppDailyPoetry.class, new AppDailyPoetryCodec());
  }

  private long millisecondsToMidnight() {
    LocalDateTime now = LocalDateTimeUtil.now();
    LocalDateTime end = LocalDateTimeUtil.endOfDay(now);
    return LocalDateTimeUtil.between(now, end, ChronoUnit.MILLIS);
  }
}
