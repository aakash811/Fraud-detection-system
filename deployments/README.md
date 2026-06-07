# Fraud Detection System - Production Deployment

## Quick Start

```bash
# Deploy with kubectl
kubectl apply -f deployments/k8s/

# Or with Helm
helm install fraud-detection charts/fraud-detection/
```

## Prerequisites

- Kubernetes 1.24+
- Helm 3.x
- ArgoCD (optional)
- Redis, PostgreSQL, Kafka (see infra/ for manifests)

## Infrastructure Setup

```bash
# Install Redis
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install redis bitnami/redis --set architecture=standalone

# Install PostgreSQL  
helm install postgres bitnami/postgresql --set auth.postgresPassword=secret

# Install Kafka
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install kafka bitnami/kafka --set replicaCount=1
```

## Configuration

Set these environment variables in your deployment:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | kafka:9092 |
| `SPRING_DATA_REDIS_HOST` | Redis host | redis |
| `SPRING_DATA_REDIS_PORT` | Redis port | 6379 |

## Health Checks

All services expose `/actuator/health` and `/actuator/prometheus` endpoints.

## Monitoring

Prometheus metrics are available at `/actuator/prometheus`.

Alert rules are defined in `monitoring.yaml`.