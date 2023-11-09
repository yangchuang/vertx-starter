package com.apps.sky.starter;

import com.origin.starter.app.spi.OriginTask;
import com.origin.starter.web.OriginWebApplication;
import com.origin.starter.web.spi.OriginRouter;
import io.vertx.core.AbstractVerticle;

import java.util.ServiceLoader;

public class Main extends AbstractVerticle {

  public static void main(String[] args) {
    ServiceLoader<OriginRouter> loader = ServiceLoader.load(OriginRouter.class);
    ServiceLoader<OriginTask> task = ServiceLoader.load(OriginTask.class);
    OriginWebApplication.runAsSingle(Main.class);
  }
}
