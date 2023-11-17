package com.apps.sky.starter.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@Slf4j
public class AppUtil {
  /**
   * @param imageUrl e.g. "https://skytools.cn/images/poetry/3.jpeg"
   * @param destinationPath e.g. "/var/www/html/images/3.png"
   * @throws IOException
   */
  public static void saveImage(String imageUrl, String destinationPath) throws IOException {
    TimeInterval timer = DateUtil.timer();
    // 创建URL对象
    URL url = new URL(imageUrl);
    // 创建目标文件
    FileUtil.touch(destinationPath);

    // 打开网络连接
    try (InputStream in = url.openStream();
         ReadableByteChannel channel = Channels.newChannel(in);
         FileOutputStream out = new FileOutputStream(destinationPath)) {
      // 使用NIO的Channel将输入流写入文件
      out.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
    }
    log.warn("从URL获取图片保存到文件系统中耗时：{} s", timer.interval()/1000);
  }
}
