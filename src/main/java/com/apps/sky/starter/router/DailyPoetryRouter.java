package com.apps.sky.starter.router;

import com.apps.sky.starter.vo.DailyPoetryVO;
import com.origin.starter.web.domain.OriginConfig;
import com.origin.starter.web.domain.OriginVertxContext;
import com.origin.starter.web.spi.OriginRouter;

import java.util.Arrays;

public class DailyPoetryRouter implements OriginRouter {
  @Override
  public void router(OriginVertxContext originVertxContext, OriginConfig originConfig) {
    originVertxContext.getRouter().get("/daily-poetry/:date").handler(ctx -> {
      String date = ctx.pathParam("date");
      //TODO: query from database
      DailyPoetryVO vo = DailyPoetryVO.builder()
        .date(date)
        .aphorism("“人生最美妙的风景，竟是内心的淡定与从容。我们曾如此期盼外界的认可，到最后才知道，世界是自己的，与他人毫无关系。” ")
        .aphorismAuthor("杨绛")
        .poetry("醉后不知天在水，满船清梦压星河")
        .imgList(Arrays.asList("https://skytools.cn/images/poetry/1.jpeg", "https://skytools.cn/images/poetry/2.jpeg"))
        .build();
      //response json
      ctx.json(vo);
    });
  }
}
