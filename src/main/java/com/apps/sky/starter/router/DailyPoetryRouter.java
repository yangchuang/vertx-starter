package com.apps.sky.starter.router;

import cn.hutool.core.lang.UUID;
import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.Request;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DailyPoetryRouter implements OriginRouter {


  private WebClient httpClient;

  private static final String CODE_2_SESSION_URI = " https://api.weixin.qq.com/sns/jscode2session?appid={wx_app_id}&secret={wx_app_secret}&js_code={code}&grant_type=authorization_code";

  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    //初始化http,redis,sql client
    initClient(originVertxContext, originConfig);

    Router router = originVertxContext.getRouter();

    router.get("/api/health").handler(healthCheckHandler(originVertxContext));
    router.get("/api/login").handler(loginHandler());
    router.get("/daily-poetry/:date").handler(dailyPoetryHandler());
    //清除cache
    router.get("/api/remove-cache").handler(removeCache());
  }


  private Handler<RoutingContext> removeCache() {
    return ctx -> {
      String code = ctx.request().params().get("access_key");
      String redisKey = ctx.request().params().get("redis_key");
      if (code != null && code.equals(System.getenv("api_access_key"))) {
        Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
        redis.connect().onSuccess(conn -> {
          conn.send(Request.cmd(Command.DEL).arg(redisKey))
            .onSuccess(res -> ctx.response().end(res.toString()))
            .onFailure(err -> ctx.fail(500, err));
        }).onComplete(ar -> ar.result().close());
      } else {
        ctx.fail(403);
      }
    };
  }

  private Handler<RoutingContext> loginHandler() {
    return ctx -> {
      String code = ctx.request().params().get("code");
      code2Session(code);
      ctx.json(new JsonObject().put("token", UUID.fastUUID().toString()));
    };
  }

  private void code2Session(String code) {
    String appId = System.getenv("wx_app_id");
    String appSecret = System.getenv("wx_app_secret");

    httpClient.getAbs(CODE_2_SESSION_URI.replace("{wx_app_id}", appId).replace("{wx_app_secret}", appSecret).replace("{code}", code))
      .send()
      .onSuccess(res -> {
        JsonObject json = res.bodyAsJsonObject();
        saveOrUpdateUser(json);
      }).onFailure(err -> {
        log.error("从微信code2Session接口失败，code：{},{}", code, err.getMessage());
      });
  }

  private void saveOrUpdateUser(JsonObject jsonObject) {
    log.info("开始保存或更新user，{}", jsonObject);
    String openId = jsonObject.getString("openid");
    String unionid = jsonObject.getString("unionid");
    String sessionKey = jsonObject.getString("session_key");
    String sql = "insert into app_daily_poetry_user(open_id, union_id, session_key) values($1, $2, $3)  " +
      "ON CONFLICT (open_id) DO update set session_key = $4, last_login_time = now(), update_time = now()";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(openId, unionid, sessionKey, sessionKey)).onComplete(rs -> {
      if (rs.succeeded()) {
        log.info("保存或更新user成功，{}", jsonObject);
      } else  {
          log.error("保存或更新user失败，{},{}", jsonObject, rs.cause());
      }
      sqlClient.close();
    });

  }

  private Handler<RoutingContext> dailyPoetryHandler() {
    return ctx -> {
      String date = ctx.pathParam("date");
      Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
      redis.connect().onSuccess(conn -> {
        conn.send(Request.cmd(Command.GET).arg(date)).onSuccess(value -> {
          if (value != null) {
            log.info("{} daily poetry cache hit", date);
            ctx.json(Json.decodeValue(value.toString()));
          } else {
            log.warn("{} daily poetry cache NOT HIT", date);
            getDailyPoetryFromDB(ctx, date);
          }
        });
      }).onFailure(ex -> {
        log.error("Failed to connect to Redis: {}", ex.getMessage());
        //连接redis失败，直接查询数据库
        getDailyPoetryFromDB(ctx, date);
      }).onComplete(ar -> ar.result().close());

    };
  }

  private void getDailyPoetryFromDB(RoutingContext ctx, String date) {
    String sql = "select date, content, title, COALESCE(dynasty, '') as dynasty, COALESCE(author, '') as author, origin_content, img_list, has_audio from app_daily_poetry where date = $1 limit 1";
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    sqlClient.preparedQuery(sql).execute(Tuple.of(date))
      .onComplete(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          if (rows.size() != 0) {
            Row row = rows.iterator().next();
            JsonObject jsonObject = row.toJson();
            Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
            //放到缓存中，24小时后失效
            redis.connect().onSuccess(conn -> {
              conn.send(Request.cmd(Command.SET, date, Json.encode(jsonObject), "EX", "86400"));
              //TODO: conn 需不需要关闭？
            }).onComplete(arConn -> arConn.result().close());

            //response json
            ctx.json(jsonObject);
          } else {
            log.warn("data not found on date:" + date);
            ctx.fail(404, new Throwable("data not found on date:" + date));
          }
        } else {
          log.error(("get daily poetry failed: " + ar.cause().getMessage()));
          ctx.fail(500, ar.cause());
        }
        sqlClient.close();
      }).onFailure(err -> ctx.fail(500, err));
  }



  private HealthCheckHandler healthCheckHandler(OriginVertxContext originVertxContext) {
    Vertx vertx = originVertxContext.getVertx();

    HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx);
    // Register procedures
    // It can be done after the route registration, or even at runtime
    healthCheckHandler.register("redis", promise -> {
      Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
      redis.connect().onSuccess(redisConnection -> {
        promise.complete(Status.OK());
        redisConnection.close();
      }).onFailure(ex -> {
        promise.complete(Status.KO(new JsonObject().put("error", ex.getLocalizedMessage())));
      }).onComplete(ar -> ar.result().close());
    });

    return healthCheckHandler;
  }

  private void initClient(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    log.info("初始化Clients");
    Vertx vertx = originVertxContext.getVertx();
    this.httpClient = WebClient.create(vertx);
  }

}
