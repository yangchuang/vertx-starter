### 主机环境：腾讯云、Ubuntu 22.04 LTS、openjdk "11.0.20.1"

#### 1. 安装[docker](https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository)

#### 2. 使用docker安装redis
```bash
docker pull redis:latest
docker images
#-p 6379:6379：映射容器服务的 6379 端口到宿主机的 6379 端口。外部可以直接通过宿主机ip:6379 访问到 Redis 的服务。
docker run -itd --name redis -p 6379:6379 redis
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
source ~/.bashrc
```

