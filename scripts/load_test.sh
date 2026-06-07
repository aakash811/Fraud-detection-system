#!/bin/bash

echo "Running load tests for Fraud Detection System"

echo "1. Submitting legitimate transactions..."
for i in {1..100}; do
  curl -s -X POST http://localhost:8080/api/v1/transactions \
    -H "Content-Type: application/json" \
    -d '{"cardLast4":"1234","cardBin":"411111","amount":50.00,"currency":"USD","merchantId":"MERCH001","merchantMcc":"5411","ipAddress":"192.168.1.'$i'"}' > /dev/null &
done
wait

echo "2. Submitting high-velocity transactions (same card)..."
for i in {1..50}; do
  curl -s -X POST http://localhost:8080/api/v1/transactions \
    -H "Content-Type: application/json" \
    -d '{"cardLast4":"BLOCKED","cardBin":"411111","amount":100.00,"currency":"USD","merchantId":"MERCH002","ipAddress":"10.0.0.1"}' > /dev/null &
done
wait

echo "3. Submitting blacklisted card transactions..."
for i in {1..30}; do
  curl -s -X POST http://localhost:8080/api/v1/transactions \
    -H "Content-Type: application/json" \
    -d '{"cardLast4":"BLACK","cardBin":"411111","amount":500.00,"currency":"USD","merchantId":"MERCH003","ipAddress":"10.0.0.2"}' > /dev/null &
done
wait

echo "Load test completed. Check Prometheus at http://localhost:9090 for metrics."