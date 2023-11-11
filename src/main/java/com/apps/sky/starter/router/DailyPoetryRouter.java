package com.apps.sky.starter.router;

import com.apps.sky.starter.vo.DailyPoetryVO;
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
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class DailyPoetryRouter implements OriginRouter {

  private SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();

  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

    Router router = originVertxContext.getRouter();

    router.get("/api/health").handler(healthCheckHandler(originVertxContext));
    router.get("/daily-poetry/:date").handler(dailyPoetryHandler());

  }

  private Handler<RoutingContext> dailyPoetryHandler() {
    return ctx -> {
      String date = ctx.pathParam("date");
      //TODO: 默认连接 127.0.0.1:6379, config.json 的配置不生效（bug或尚未实现）
      Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
      redis.connect();
      RedisAPI redisAPI = RedisAPI.api(redis);
      redisAPI.get(date).onSuccess(value -> {
        if (value != null) {
          log.info("cache hit");
          ctx.json(Json.decodeValue(value.toString()));
        } else {
          log.warn("XXX cache not hit");
          //TODO: query from database
          DailyPoetryVO vo = DailyPoetryVO.builder()
            .date(date)
            .aphorism("“人生最美妙的风景，竟是内心的淡定与从容。我们曾如此期盼外界的认可，到最后才知道，世界是自己的，与他人毫无关系。” ")
            .aphorismAuthor("杨绛")
            .poetry("醉后不知天在水，满船清梦压星河")
            .imgList(Arrays.asList("https://skytools.cn/images/poetry/1.jpeg", "https://skytools.cn/images/poetry/2.jpeg"))
            .build();
          //set cache, expire time 24h
          redisAPI.set(Arrays.asList(date, Json.encode(vo), "EX", "86400"));

          //response json
          ctx.json(vo);
        }
      }).onComplete(handle -> {
        redisAPI.close();
        //redis.close(); //redisAPI.close() 会把redis 关闭
      });
    };
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
      });
    });

    return healthCheckHandler;
  }

}
