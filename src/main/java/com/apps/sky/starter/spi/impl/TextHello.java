package com.apps.sky.starter.spi.impl;

import com.apps.sky.starter.spi.HelloSPI;

public class TextHello implements HelloSPI {
  @Override
  public void sayHello() {
    System.out.println("Text");
  }
}
