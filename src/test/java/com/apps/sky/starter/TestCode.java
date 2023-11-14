package com.apps.sky.starter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TestCode {
  @Test
  void test() {
    long seconds = calculateSecondsToMidnight();
    System.out.println(seconds);

    LocalDateTime now = LocalDateTimeUtil.now();
    LocalDateTime end = LocalDateTimeUtil.endOfDay(now);
    long duration = LocalDateTimeUtil.between(now, end, ChronoUnit.SECONDS);
    System.out.println(duration);
  }

  public static long calculateSecondsToMidnight() {
    LocalTime currentTime = LocalTime.now();
    LocalTime midnight = LocalTime.MAX;

    long secondsToMidnight = currentTime.until(midnight, ChronoUnit.SECONDS);
    return secondsToMidnight;
  }

  @Test
  void testError() {
    try {
      int i = 1/0;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println(e.getCause().getMessage());
    }
  }

  @Test
  void testNowStr() {
    System.out.println(DateUtil.today());
  }

}
