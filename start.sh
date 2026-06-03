#!/bin/bash

APP_DIR=/home/www/keling-tts/app

# 阿里云 OSS 配置
export OSS_ACCESS_KEY_ID=LTAI5tGJDzWwRJZbwPWHngRQ
export OSS_ACCESS_KEY_SECRET=3XKuKbOPjaJIhzu1xVgPT0OhcnJgvP
export OSS_BUCKET_NAME=wangke-boke1
export OSS_ENDPOINT=oss-cn-shanghai.aliyuncs.com

# 阿里云 DashScope API Key
export DASHSCOPE_API_KEY=sk-2dd184cd1a5d4a5492db9b5c4bf83824

cd $APP_DIR
java -jar keling-tts.jar --spring.profiles.active=prod
