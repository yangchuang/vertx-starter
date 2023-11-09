package com.apps.sky.starter.task;

import com.origin.starter.app.domain.OriginAppConfig;
import com.origin.starter.app.domain.OriginAppVertxContext;
import com.origin.starter.app.spi.OriginTask;

public class ImageGenerator implements OriginTask {
  @Override
  public void run(OriginAppVertxContext originAppVertxContext, OriginAppConfig originAppConfig) {
    originAppVertxContext.getVertx().setPeriodic(5000, t -> {
      System.out.println(t+"Hello");
    });
  }
}
