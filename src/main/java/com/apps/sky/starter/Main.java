package com.apps.sky.starter;

import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.AbstractVerticle;

import java.util.ServiceLoader;

public class Main extends AbstractVerticle {

  public static void main(String[] args) {
    OriginWebApplication.runAsSingle(Main.class);
  }
}
