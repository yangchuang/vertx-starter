### “诗画共赏”小程序后台代码
“诗画共赏”是一个展示唐诗宋词和DALL-E3 AI配图的小程序。每天，你可以在这里欣赏到一首精选的唐诗宋词，以及根据诗句生成的DALL-E3 AI配图。
#### 运行截图
<img src="./images/screen_shot.jpeg" alt="诗画共赏" width=300px/>

#### 体验小程序⬇️
<img src="./images/gh_71c17530cffe_1280.jpg" alt="诗画共赏" width=300px/>

前端代码地址：[poetry_with_dalle_art](https://github.com/yangchuang/poetry_with_dalle_art)

### 后台部署主机环境：腾讯云Ubuntu 22.04 LTS、openjdk-17-sdk

#### 1. 安装[docker](https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository)

#### 2. 使用docker安装redis
```bash
docker pull redis:latest
docker images
#-p 6379:6379：映射容器服务的 6379 端口到宿主机的 6379 端口。外部可以直接通过宿主机ip:6379 访问到 Redis 的服务。数据目录挂载到服务器/data/redis 目录下
# docker run -itd --name redis -p 6379:6379 redis
docker run -d --name redis-container -v /data/redis:/data --restart=always -p 6379:6379 redis
docker ps
#通过 redis-cli 连接测试使用 redis 服务
docker exec -it redis /bin/bash
redis-cli
# 重启容器
docker start [container id]
```

#### 3. 使用docker安装postgresql
```bash
docker pull postgres
docker images
# 在运行 Docker 的系统中，创建一个可以挂在 PostgreSQL 数据文件的地方，方便后面做数据迁移等工作。
# 使用/data/postgres 当作挂在文件的目录
mkdir -p /data/postgres
#启动镜像，对外端口号 15866
docker run --name postgresql --privileged -e POSTGRES_PASSWORD={这里设置密码} -p 15866:5432 -v /data/postgres:/var/lib/postgresql/data -d postgres
#查看日志
docker logs [container id]
#查看进程
docker ps -a
# 重启容器
docker start [container id]
```

#### 4. 创建数据库并导入数据表
```shell
# 见/sql/init/create_database_and_user.sql及/sql/migration/*
```

#### 5.设置系统环境变量
```shell
sudo -s
echo 'export OPENAI_API_KEY="sk-****"' >> ~/.bashrc
echo 'export JRSC_TOKEN="****Rsk"' >> ~/.bashrc
echo 'export wx_app_id="wx***"'  >> ~/.bashrc
echo 'export wx_app_secret="d268d****"'  >> ~/.bashrc
source ~/.bashrc
```

#### 6. 定时对DALL-E生成的配图进行压缩，见 [图片压缩](./IMAGE_COMPRESS.md)

#### 7. 采集数据，部署一套[古诗词API](https://github.com/xenv/gushici)

### 感谢🙏
1. 底层框架使用了[Origin](https://github.com/kxu913/origin) framework, 简化了vertx的开发。
2. 使用[古诗词API](https://github.com/xenv/gushici)，自己部署了一套服务，数据集来源于 [花间集](https://github.com/chinese-poetry/huajianji)的诗词部分
3. 使用[ChatGPT-Next-Web](https://github.com/Yidadaa/ChatGPT-Next-Web) 部署到vercel作为代理使国内的服务器可以访问OpenAI的DALL-E API

### 灵感来源
- [yihong0618/2023](https://github.com/yihong0618/2023)
