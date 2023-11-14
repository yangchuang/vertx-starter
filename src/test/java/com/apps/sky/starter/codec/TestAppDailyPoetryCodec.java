package com.apps.sky.starter.codec;


import com.apps.sky.starter.Main;
import com.apps.sky.starter.domain.AppDailyPoetry;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;

@Disabled
@ExtendWith(VertxExtension.class)
public class TestAppDailyPoetryCodec {
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new Main(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void test(Vertx vertx, VertxTestContext testContext) {
    EventBus eventBus = vertx.eventBus();
    eventBus.registerDefaultCodec(AppDailyPoetry.class, new AppDailyPoetryCodec());
    eventBus.consumer("generate-poetry-image-done", message -> {
      handle(message, vertx);
    });

    AppDailyPoetry poetry = new AppDailyPoetry();
    poetry.setDate("2023-11-14");
    poetry.setImgList(Arrays.asList("1.png"));
    // 发送消息
    eventBus.send("generate-poetry-image-done", poetry);
  }

  private static void handle(Message<Object> message, Vertx vertx) {
    AppDailyPoetry poetry = (AppDailyPoetry) message.body();
    System.out.println(poetry.getImgList().get(0));
    vertx.close();
  }
}
