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
