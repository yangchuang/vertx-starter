package com.apps.sky.starter.vo;


import lombok.Builder;

import java.util.List;

@Builder
public class DailyPoetryVO {
  private String date;
  private String aphorism;
  private String aphorismAuthor;
  private String poetry;
  private String poetryAuthor;
  private List<String> imgList;

}
