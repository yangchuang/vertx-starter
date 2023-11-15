DELL-E3生成的配图比较大，需要对其进行压缩。采取的策略是将DELL-E3生成的配图已png格式保存到服务器，然后定期执行一个脚本将其压缩，并重命名为jpg格式。

1. Ubuntu下安装 imagemagick，使用`convert input.png -quality 80 output.jpg`命令进行压缩。-quality 80表示以80%的质量进行压缩。
```shell
apt install imagemagick-6.q16hdri
```

2. 脚本如下，如果png图片已经压缩过，也就是说如果该目录下已经存在同名的jpg图片，则不再对png图片其进行压缩。将其保存为compress_png_to_jpg.sh文件，并将其赋予执行权限（chmod +x compress_png_to_jpg.sh）
```shell
#!/bin/bash

# 设置目标目录
target_dir="/var/www/html/images/poetry"

# 获取当前日期
current_date=$(date +%Y-%m-%d)

# 查找目标目录及其子目录下的所有png文件
find "$target_dir" -type f -name "*.png" | while read -r filepath; do
    # 获取文件所在目录
    filedir=$(dirname "$filepath")

    # 获取文件名（不包含扩展名）
    filename=$(basename "$filepath" .png)

    # 检查是否已经存在同名的jpg文件，如果存在则跳过
    if [ -f "$filedir/$filename.jpg" ]; then
        echo "跳过已经压缩过的文件：$filedir/$filename.png"
        continue
    fi

    # 压缩并转换为jpg格式，并保存到指定目录
    convert "$filepath" -quality 80 "$filedir/$filename.jpg"

    # 不删除原始的png文件
    # rm "$filepath"

    # 输出处理的文件名
    echo "压缩并保存为：$filedir/$filename.jpg"
done
```

3. 运行以下命令来编辑cron表达式，在打开的文件中，加入`1 0 * * * /path/to/compress_png_to_jpg.sh`来设置每天午夜0点1分运行该脚本，保存并退出文件即可。

```shell
crontab -e
```
