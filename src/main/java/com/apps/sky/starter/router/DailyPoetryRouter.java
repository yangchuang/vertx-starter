package com.apps.sky.starter.router;

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

  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    Router router = originVertxContext.getRouter();

    router.get("/api/health").handler(healthCheckHandler(originVertxContext));
    router.get("/daily-poetry/:date").handler(dailyPoetryHandler());

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
      });

    };
  }

  private void getDailyPoetryFromDB(RoutingContext ctx, String date) {
    SqlClient sqlClient = OriginWebApplication.getBeanFactory().getSqlClient();
    String sql = "select date, content, title, dynasty, author, origin_content, img_list from app_daily_poetry where date = $1 limit 1";
    sqlClient.preparedQuery(sql).execute(Tuple.of(date))
      .onComplete(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> rows = ar.result();
          if (rows.size() != 0) {
            Row row = rows.iterator().next();
            JsonObject jsonObject = row.toJson();

            //放到缓存中，24小时后失效
            Redis redis = OriginWebApplication.getBeanFactory().getRedisClient();
            redis.connect().onSuccess(conn -> {
              conn.send(Request.cmd(Command.SET, date, Json.encode(jsonObject), "EX", "86400"));
              //TODO: conn 需不需要关闭？
            });

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
        //onComplete无论成功还是失败，都关闭sqlCLient
        //TODO: 但这个看起来不像是释放数据库连接？
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
      });
    });

    return healthCheckHandler;
  }

}
