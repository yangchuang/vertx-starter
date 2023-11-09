package com.apps.sky.starter;

import com.apps.sky.starter.spi.HelloSPI;
import com.origin.starter.web.OriginWebApplication;
import io.vertx.core.AbstractVerticle;

import java.util.ServiceLoader;

public class Main extends AbstractVerticle {

  public static void main(String[] args) {
    ServiceLoader<HelloSPI> serviceLoader = ServiceLoader.load(HelloSPI.class);
    // 执行不同厂商的业务实现，具体根据业务需求配置
    for (HelloSPI helloSPI : serviceLoader) {
      helloSPI.sayHello();
    }
    OriginWebApplication.runAsSingle(Main.class);
  }
}
