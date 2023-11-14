package com.apps.sky.starter;

import com.apps.sky.starter.utils.AppUtil;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class TestImageDownload {

  @Test
  void test() {
//    String imageUrl = "https://skytools.cn/images/poetry/1.jpeg";
    String imageUrl = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-dlb9I1AyYbikUMJvFGvynnQg/user-FRDICW11KVNkgeTc0kJqZHU8/img-uCqJ8o3XYvbSM8c7XMEs54rM.png?st=2023-11-14T07%3A11%3A32Z&se=2023-11-14T09%3A11%3A32Z&sp=r&sv=2021-08-06&sr=b&rscd=inline&rsct=image/png&skoid=6aaadede-4fb3-4698-a8f6-684d7786b067&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2023-11-13T16%3A17%3A21Z&ske=2023-11-14T16%3A17%3A21Z&sks=b&skv=2021-08-06&sig=Oj6DzOcgRZX1EjZLsSf8MTObF3dbB2c55/aHyeSpzU0%3D";
    String destinationPath = "/Users/yang/data/p11.jpeg";

    try {
      AppUtil.saveImage(imageUrl, destinationPath);
      System.out.println("图片保存成功！");
    } catch (IOException e) {
      System.out.println("图片保存失败：" + e.getMessage());
    }

  }

  public void saveImage(String imageUrl, String destinationPath) throws IOException {
    // 创建URL对象
    URL url = new URL(imageUrl);

    // 打开网络连接
    try (InputStream in = url.openStream();
         ReadableByteChannel channel = Channels.newChannel(in);
         FileOutputStream out = new FileOutputStream(destinationPath)) {

      // 使用NIO的Channel将输入流写入文件
      out.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
    }
  }
}
