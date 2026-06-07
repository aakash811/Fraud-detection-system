# Fraud Detection System

A production-grade real-time payment fraud detection platform built with Java Spring Boot. The system intercepts payment transactions, applies fraud detection rules, and enforces allow/block/throttle decisions with sub-50ms latency.

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Payment Source │────▶│  Ingestor       │     │  Alert Manager  │
│  (POS, Online) │     │  (8080)         │────▶│  (SMS/Email)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                │
                                ▼
                        ┌─────────────────┐
                        │  Kafka          │
                        │  transactions.v1│
                        └─────────────────┘
                                │
               ┌──────────────────┼──────────────────┐
               ▼                  ▼                  ▼
┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
│  Fraud Analyzer │   │  Redis Cache    │   │  Compliance DB  │
│  (8081)         │   │  (Rules State)  │   │  (PostgreSQL)   │
└─────────────────┘   └─────────────────┘   └─────────────────┘
               │                  │
               ▼                  ▼
┌─────────────────┐   ┌─────────────────┐
│  Decisions      │   │  Metrics        │
│  (Decisions API)│   │  (Prometheus)   │
└─────────────────┘   └─────────────────┘
```

## Services

| Service | Port | Responsibility |
|---------|------|----------------|
| payment-ingestor | 8080 | REST API for transaction submission |
| fraud-analyzer | 8081 | Real-time fraud detection engine |
| compliance-auditor | 8082 | Persistent audit storage |
| risk-orchestrator | 8083 | Rule management API |
| alert-dispatcher | 8084 | High-risk alerts (SMS/Email) |

## Detection Rules

1. **VelocityRule** (weight: 0.25) - Detects high transaction frequency per card
2. **BlacklistRule** (weight: 0.30) - Flags known bad card numbers
3. **GeolocationRule** (weight: 0.20) - Identifies impossible travel patterns
4. **TimeBasedRule** (weight: 0.15) - Suspicious hours (2-5 AM)
5. **AmountAnomalyRule** (weight: 0.20) - Statistical outlier detection
6. **DeviceFingerprintRule** (weight: 0.10) - Multiple cards from same device
7. **MerchantPatternRule** (weight: 0.10) - Rapid merchant switching
8. **IpVelocityRule** (weight: 0.15) - High transaction count per IP

## Scoring Engine

```
Score Calculation: Σ(ruleScore × ruleWeight)
High-Risk Override: Any rule score ≥ 0.9 triggers immediate BLOCK

Thresholds:
- Score < 0.30  → ALLOW
- 0.30-0.50     → FLAG (logging only)
- 0.50-0.75     → THROTTLE (rate limited)
- > 0.75        → BLOCK
```

## Transaction Event Fields

| Field | Description |
|-------|-------------|
| cardLast4 | Last 4 digits of card |
| cardBin | Card BIN (first 6 digits) |
| userId | Optional user identifier |
| accountId | Optional account identifier |
| amount | Transaction amount |
| currency | ISO currency code |
| merchantId | Merchant identifier |
| merchantMcc | Merchant MCC code |
| ipAddress | Client IP address |
| deviceId | Device fingerprint |
| latitude/longitude | Geolocation |
| correlationId | For tracing transactions across services |
| idempotencyKey | For deduplication |

## Investigation API

```
GET /api/v1/investigations              # List pending investigations
GET /api/v1/investigations/{cardLast4}  # Get decision history for card
GET /api/v1/investigations/stats        # Get fraud statistics
PUT /api/v1/investigations/{id}/status  # Update investigation status
POST /api/v1/investigations/blacklist/{cardLast4}  # Add to blacklist
DELETE /api/v1/investigations/blacklist/{cardLast4} # Remove from blacklist
GET /api/v1/risk/decision?cardLast4=... # Get cached decision
GET /api/v1/risk/rules                # Get current rule configuration
PUT /api/v1/risk/rules/weights        # Update rule weights dynamically
```

## Tech Stack

- **Java 21** - Virtual threads, modern features
- **Spring Boot 3.3.x** - Latest stable version
- **Apache Kafka 3.7** - Event streaming backbone
- **Redis 7.2** - Real-time counters and caching
- **PostgreSQL 16** - Audit trail storage
- **Micrometer** - Prometheus metrics

## Quick Start

```bash
# Build all modules
mvn clean install

# Start infrastructure
docker compose -f deployments/docker-compose.yml up -d

# Run services (5 terminals)
mvn spring-boot:run -pl payment-ingestor
mvn spring-boot:run -pl fraud-analyzer
mvn spring-boot:run -pl compliance-auditor
mvn spring-boot:run -pl risk-orchestrator
mvn spring-boot:run -pl alert-dispatcher

# Seed Kafka topics
./scripts/seed_kafka_topics.sh       # Bash
./scripts/seed_kafka_topics.ps1      # PowerShell

# Test transaction
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"cardLast4":"1234","cardBin":"411111","amount":50.00,"currency":"USD","merchantId":"MERCH001","ipAddress":"192.168.1.1"}'
```

## Kafka Topics

- `transactions.v1` - Incoming transaction events
- `decisions.v1` - Fraud decision events
- `fraud-alerts.v1` - High-risk alert events for downstream consumers

## Testing

```bash
# Unit tests
mvn test
```

## License

MIT License
