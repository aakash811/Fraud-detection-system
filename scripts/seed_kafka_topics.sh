#!/usr/bin/env bash
set -e

COMPOSE_FILE="deployments/docker-compose.yml"

docker compose -f "$COMPOSE_FILE" exec -T kafka kafka-topics --create --topic transactions.v1 \
  --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1 --if-not-exists

docker compose -f "$COMPOSE_FILE" exec -T kafka kafka-topics --create --topic decisions.v1 \
  --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1 --if-not-exists

docker compose -f "$COMPOSE_FILE" exec -T kafka kafka-topics --create --topic fraud-alerts.v1 \
  --bootstrap-server localhost:9092 --partitions 6 --replication-factor 1 --if-not-exists

echo "Kafka topics created successfully"
