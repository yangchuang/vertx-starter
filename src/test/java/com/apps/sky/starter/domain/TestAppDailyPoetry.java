package com.apps.sky.starter.domain;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class TestAppDailyPoetry {
  @Test
  void test() {
    JsonObject json = new JsonObject(
      "{\"status\":\"success\",\"data\":{\"content\":\"君问归期未有期，巴山夜雨涨秋池。\",\"popularity\":1170000,\"origin\":{\"title\":\"夜雨寄北\",\"dynasty\":\"唐代\",\"author\":\"李商隐\",\"content\":[\"君问归期未有期，巴山夜雨涨秋池。\",\"何当共剪西窗烛，却话巴山夜雨时。\"]},\"matchTags\":[\"秋\",\"晚上\"]},\"ipAddress\":\"162.248.93.154\"}"
    );

    JsonObject data = json.getJsonObject("data");
    JsonObject origin = data.getJsonObject("origin");

    AppDailyPoetry poetry = new AppDailyPoetry();
    poetry.setContent(data.getString("content"));
    poetry.setPopularity(data.getInteger("popularity"));
    poetry.setTitle(origin.getString("title"));
    poetry.setDynasty(origin.getString("dynasty"));
    poetry.setAuthor(origin.getString("author"));
    poetry.setOriginContent(origin.getJsonArray("content").getList());
    poetry.setMatchTags(data.getJsonArray("matchTags").getList());
    poetry.setIpAddress(json.getString("ipAddress"));

    System.out.println(poetry);
  }

  @Test
  void testOpenAIResp() {
    String json = "{\"created\": 1699949492,\"data\": [{\"revised_prompt\": \"Chinese ink painting: A person sitting and observing a knight from Yu state, embarking on a long journey through the areas of the Yellow River and Luo River in dusk.\",\"url\": \"https://oaidalleap.com\"}]}";
    JsonObject jsonObject = new JsonObject(json);
    String url = jsonObject.getJsonArray("data")
      .getJsonObject(0)
      .getString("url");
    System.out.println("URL: " + url);
  }
}
