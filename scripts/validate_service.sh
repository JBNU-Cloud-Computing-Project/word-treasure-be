#!/bin/bash

echo "🔍 Validating service..."

# 최대 30초 대기
for i in {1..30}; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health 2>/dev/null)

    if [ "$HTTP_CODE" == "200" ]; then
        echo "✅ Service is healthy! (HTTP $HTTP_CODE)"
        exit 0
    fi

    echo "Waiting for service... (attempt $i/30, HTTP code: $HTTP_CODE)"
    sleep 1
done

echo "❌ Service validation failed!"
exit 1