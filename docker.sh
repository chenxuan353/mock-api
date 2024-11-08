#!/bin/bash
# 应用组名
group_name='java'
# 应用名称
app_name='mock-api'
echo '---------- 停止旧容器 ----------'
docker stop ${app_name}
echo '---------- 删除旧容器 ----------'
docker rm ${app_name}
echo '---------- 删除旧镜像 ----------'
docker rmi ${group_name}/${app_name}
echo '---------- 打包新镜像 ----------'
docker build -t ${group_name}/${app_name} .
echo '---------- 删除无用镜像 ----------'
docker image prune -f
echo '---------- 创建新容器 ----------'
docker run -p 8990:8990 --name ${app_name} \
-e TZ="Asia/Shanghai" \
-d ${group_name}/${app_name}