package com.apps.sky.starter.domain;

import lombok.Data;

import java.util.List;

@Data
public class AppDailyPoetry {

  /**
   * 日期
   */
  private String date;

  /**
   * 核心诗句
   */
  private String content;

  /**
   * 诗句的流行度评价
   */
  private Integer popularity;

  /**
   * 诗句的标题
   */
  private String title;

  /**
   * 作者所属的朝代
   */
  private String dynasty;

  /**
   * 作者姓名
   */
  private String author;

  /**
   * 诗句的原始内容
   */
  private List<String> originContent;

  /**
   * 与诗句相关的标签
   */
  private List<String> matchTags;

  /**
   * 核心诗句DALL-E API创建的图片地址列表
   */
  private List<String> imgList;

  /**
   * 调用今日诗词API的IP地址
   */
  private String ipAddress;

}

