package com.apps.sky.starter.codec;

import com.apps.sky.starter.domain.AppDailyPoetry;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class AppDailyPoetryCodec  implements MessageCodec<AppDailyPoetry, AppDailyPoetry> {
  @Override
  public void encodeToWire(Buffer buffer, AppDailyPoetry poetry) {
    JsonObject json = JsonObject.mapFrom(poetry);
    String jsonStr = json.encode();
    buffer.appendInt(jsonStr.length());
    buffer.appendString(jsonStr);
  }

  @Override
  public AppDailyPoetry decodeFromWire(int pos, Buffer buffer) {
    int length = buffer.getInt(pos);
    String jsonStr = buffer.getString(pos + 4, pos + 4 + length);
    JsonObject json = new JsonObject(jsonStr);
    return json.mapTo(AppDailyPoetry.class);
  }

  @Override
  public AppDailyPoetry transform(AppDailyPoetry poetry) {
    return poetry;
  }

  @Override
  public String name() {
    return "AppDailyPoetry";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
