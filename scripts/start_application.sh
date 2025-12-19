#!/bin/bash

echo "🚀 Starting application..."

# 환경 변수 설정
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# 작업 디렉토리
cd /opt/wordtreasure

# JAR 파일 찾기
JAR_FILE=$(ls -t build/libs/*.jar 2>/dev/null | grep -v plain | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ JAR file not found!"
    exit 1
fi

echo "Found JAR: $JAR_FILE"

# 로그 디렉토리 생성
mkdir -p /opt/wordtreasure/logs

# 애플리케이션 시작
nohup java -jar \
    -Dspring.profiles.active=prod \
    -Dserver.port=8080 \
    $JAR_FILE \
    > /opt/wordtreasure/logs/application.log 2>&1 &

# PID 저장
echo $! > /opt/wordtreasure/application.pid

echo "✅ Application started with PID: $(cat /opt/wordtreasure/application.pid)"
exit 0