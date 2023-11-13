package com.apps.sky.starter.task;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
public class PoetryFetchTask implements OriginRouter {
  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {

    Vertx vertx = originVertxContext.getVertx();
    //设置00:00:10 开始取当天数据
    vertx.setTimer(millisecondsToMidnight() + 10000, timerId -> {
      log.info("begin to run daily job at {}", LocalDateTimeUtil.now());
      fetchDailyPoetry(vertx);
      //每天取一次数据
      vertx.setPeriodic(24*60*60*1000, periodicId -> {
        log.info("begin to fetch daily poetry at {}", LocalDateTimeUtil.now());
        fetchDailyPoetry(vertx);
      });
    });
  }

  private void fetchDailyPoetry(Vertx vertx) {
    //从环境变量中取今日诗词API的token，https://www.jinrishici.com/doc/#get-token
    String jrscToken = System.getenv("JRSC_TOKEN");
  }

  private void generateImage(String poetry) {
    //从环境变量中取OpenAI的API KEY
    String openAIAPIkey = System.getenv("OPENAI_API_KEY");
  }
  private long millisecondsToMidnight() {
    LocalDateTime now = LocalDateTimeUtil.now();
    LocalDateTime end = LocalDateTimeUtil.endOfDay(now);
    return LocalDateTimeUtil.between(now, end, ChronoUnit.MILLIS);
  }
}
