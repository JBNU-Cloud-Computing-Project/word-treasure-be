#!/bin/bash

echo "🛑 Stopping application..."

# PID 파일 확인
PID_FILE=/opt/wordtreasure/application.pid

if [ -f $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    echo "Found PID: $PID"

    if ps -p $PID > /dev/null 2>&1; then
        echo "Killing process $PID"
        kill -15 $PID

        # Graceful shutdown 대기 (최대 30초)
        for i in {1..30}; do
            if ! ps -p $PID > /dev/null 2>&1; then
                echo "Process stopped gracefully"
                rm -f $PID_FILE
                exit 0
            fi
            sleep 1
        done

        # 강제 종료
        echo "Force killing process $PID"
        kill -9 $PID
        rm -f $PID_FILE
    else
        echo "Process not running, cleaning up PID file"
        rm -f $PID_FILE
    fi
else
    echo "No PID file found, nothing to stop"
fi

echo "✅ Stop complete"
exit 0