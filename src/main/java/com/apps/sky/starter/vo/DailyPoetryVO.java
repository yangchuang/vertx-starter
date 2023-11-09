package com.apps.sky.starter.vo;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class DailyPoetryVO {
  private String date;
  private String aphorism;
  private String aphorismAuthor;
  private String poetry;
  private String poetryAuthor;
  private List<String> imgList;

}
