## python3 tts.py "你好，世界" "/var/www/html/audios/poetry/2023-11-11/" "1.mp3"
## 放/work/projects/sky_apps 目录下
import asyncio
import os
import edge_tts

async def generate_tts(text, store_path, file_name):
  voice = "zh-CN-XiaoxiaoNeural"

  # 检查存储路径是否存在，不存在则创建目录
  if not os.path.exists(store_path):
    os.makedirs(store_path)

  # 生成文本转语音
  communicate = edge_tts.Communicate(text, voice)
  await communicate.save(os.path.join(store_path, file_name))

if __name__ == "__main__":
  import sys

  # 获取命令行参数
  if len(sys.argv) != 4:
    print("请提供正确的参数：text store_path file_name")
    sys.exit(1)

  text = sys.argv[1]
  store_path = sys.argv[2]
  file_name = sys.argv[3]

  loop = asyncio.get_event_loop()
  try:
    loop.run_until_complete(generate_tts(text, store_path, file_name))
  finally:
    loop.close()
