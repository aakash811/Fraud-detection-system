# Real-Time Fraud Detection Platform

A production-grade system that detects fraud from payment transactions, auto-creates alerts, persists decisions in PostgreSQL, and streams everything live to an ops dashboard — all connected through Kafka.

Built to demonstrate **event-driven microservices**, **real-time streaming**, **alert correlation**, and **GitOps deployment** in a realistic fintech scenario.

---

## Architecture

```
Payment Source / Merchant
        |
        | POST /api/v1/transactions
        ↓
  Payment Ingestor (port 8080)
        |
        | publishes to Kafka: transactions.v1
        ↓
  Fraud Analyzer (port 8081)  ←── consumes transactions.v1
        |                           applies detection rules
        | publishes to Kafka: decisions.v1
        |                           fraud-alerts.v1 (high-risk)
        ↓
  Risk Orchestrator (port 8083)  ←── consumes decisions.v1
        | caches decisions in Redis
        | exposes REST API for investigations
        ↓
  Compliance Auditor (port 8082) ←── consumes decisions.v1
        | persists to PostgreSQL
        | maintains audit trail
        ↓
  Alert Dispatcher (port 8084) ←── consumes fraud-alerts.v1
        |                           (future: SMS/Email/slack)
        ↓
  Ops Dashboard (port 3000)
        |
        └── fetches existing decisions from Risk Orchestrator on load
```

---

## Why This Architecture — Interview Talking Points

### Why Kafka instead of direct service-to-service HTTP calls?

Services don't call each other at all. The Payment Ingestor has no idea the Fraud Analyzer exists — it just fires an event onto `transactions.v1`. This means:

- **Decoupling**: I can add a new consumer (e.g. a Slack notifier or ML trainer) without touching any existing service.
- **Resilience**: If the Fraud Analyzer is down, Kafka holds the messages. Once it recovers, it replays from where it left off — no transactions are lost.
- **Backpressure**: Kafka naturally absorbs traffic spikes. If a sale floods us with transactions, the Fraud Analyzer processes them at its own pace.

Compare this to HTTP: if the Fraud Analyzer is temporarily down, the Ingestor gets a 503 and the transaction is gone.

---

### Why Redis for decision caching instead of PostgreSQL?

The Risk Orchestrator needs to answer "is this card allowed?" in under 10ms:

- Redis holds the last decision in memory — **O(1) lookup**.
- PostgreSQL would add 20-50ms of disk I/O for every check, slowing down the critical path.
- Redis **TTL** automatically expires old decisions (we don't need 6-month-old cached decisions).
- We still persist everything to PostgreSQL for compliance/audit — Redis is the **hot path cache**.

---

### Why PostgreSQL instead of a NoSQL database?

Decisions are structured and require strong consistency for regulatory compliance:

- Every decision is a financial record that must be stored with **ACID guarantees**.
- Regulatory queries: "Show me all BLOCK decisions for card X in the last 7 days" need proper indexes and joins.
- PostgreSQL **TOAST** handles JSON metadata in the decision_log table for flexible schema evolution.
- If we had unstructured telemetry logs, MongoDB would be the better choice.

---

### Alert Correlation — preventing alert storms

Without correlation, a stolen card used 50 times would trigger 50 separate BLOCK events.

The Risk Orchestrator checks: **"Is there already a recent BLOCK for this card?"** before sending high-sev alerts. If yes, it deduplicates within the window. The decision count stays manageable; the audit trail still records every transaction.

This is the same pattern used by PagerDuty and Datadog — grouping alerts into a single incident reduces on-call fatigue.

```java
// RiskService.java
if (cachedDecision.getAction() == Action.BLOCK) {
    return cachedDecision; // no duplicate alert
}
```

---

### Circuit Breaker — handling Redis/PostgreSQL failures gracefully

Every write path in the Fraud Analyzer and Compliance Auditor is wrapped with Resilience4j's `@CircuitBreaker`. If Redis becomes slow or unavailable:

1. The first few failures are counted.
2. Once the threshold is crossed, the circuit **opens** — further requests fast-fail immediately instead of waiting for timeouts.
3. After a configured interval, the circuit **half-opens** to probe recovery.

Without this, services hang waiting for database timeouts, eventually starving thread pools. With it, the system degrades gracefully and recovers automatically.

---

### Prometheus Integration

The Fraud Analyzer exposes `/actuator/prometheus` for scraping:

- Micrometer metrics built into Spring Boot
- `fraud_transactions_total` - transaction counters
- `fraud_transactions_blocked_total` - blocked transaction rate
- `redis_connected` - Redis health indicator

---

### GitOps Deployment with ArgoCD

The `deployments/k8s/base/` directory contains Kubernetes manifests managed by Kustomize. ArgoCD watches the `main` branch — any merged commit that changes those manifests is **automatically deployed** to the cluster with self-healing enabled.

This means:
- No manual `kubectl apply` in CI
- Drift detection: if someone manually patches a deployment, ArgoCD reverts it
- Full audit trail: every deploy maps to a git commit

---

## Project Structure

```
fraud-detection-system/
├── payment-ingestor/    → REST API for transaction submission (port 8080)
├── fraud-analyzer/      → Real-time fraud detection engine (port 8081)
├── compliance-auditor/  → Persistent audit storage (port 8082)
├── risk-orchestrator/   → Decision cache & investigation API (port 8083)
├── alert-dispatcher/    → High-risk alert dispatcher (port 8084)
├── common/              → Shared Kafka event schema
└── deployments/
    ├── docker-compose.yml → Local development stack
    ├── k8s/              → Kubernetes manifests (Kustomize)
    ├── argocd-app.yaml   → ArgoCD GitOps config
    ├── argocd-appset.yaml→ ArgoCD ApplicationSet for multi-env
    └── charts/           → Helm chart for alternative deployment
```

---

## The Five Backend Services

### 1. Payment Ingestor — port 8080

Accepts transactions from any source and publishes them to Kafka. Stateless — no database.

**Endpoints:**

- `POST /api/v1/transactions`

  ```json
  {
    "cardLast4": "1234",
    "cardBin": "411111",
    "userId": "user-123",
    "accountId": "acc-456",
    "amount": 50.00,
    "currency": "USD",
    "merchantId": "MERCH001",
    "merchantMcc": "5411",
    "merchantName": "Grocery Store",
    "ipAddress": "192.168.1.1",
    "deviceId": "device-abc",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "transactionId": "tx-789",
    "correlationId": "corr-001",
    "idempotencyKey": "idem-001"
  }
  ```

Publishes to:
- `transactions.v1` — consumed by Fraud Analyzer

---

### 2. Fraud Analyzer — port 8081

The core detection engine. Consumes transactions, applies 8 fraud rules, produces decisions.

**Detection Rules:**

| Rule | Weight | Description |
|------|--------|-------------|
| VelocityRule | 0.25 | High transaction frequency per card |
| BlacklistRule | 0.30 | Known bad card numbers |
| GeolocationRule | 0.20 | Impossible travel patterns |
| TimeBasedRule | 0.15 | Suspicious hours (2-5 AM) |
| AmountAnomalyRule | 0.20 | Statistical outlier detection |
| DeviceFingerprintRule | 0.10 | Multiple cards from same device |
| MerchantPatternRule | 0.10 | Rapid merchant switching |
| IpVelocityRule | 0.15 | High transaction count per IP |

**Scoring Engine:**

```
Score Calculation: Σ(ruleScore × ruleWeight)
High-Risk Override: Any rule score ≥ 0.9 triggers immediate BLOCK

Thresholds:
- Score < 0.30   → ALLOW
- 0.30-0.50      → FLAG (logging only)
- 0.50-0.75      → THROTTLE (rate limited)
- > 0.75         → BLOCK
```

Publishes to:
- `decisions.v1` — consumed by Risk Orchestrator & Compliance Auditor
- `fraud-alerts.v1` — consumed by Alert Dispatcher (score ≥ 0.9)

---

### 3. Risk Orchestrator — port 8083

Decision cache and investigation management. Consumes decisions, caches in Redis, exposes REST API.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/risk/decision?cardLast4=...` | Get cached decision |
| `GET` | `/api/v1/investigations` | List pending investigations |
| `GET` | `/api/v1/investigations/{cardLast4}` | Get decision history for card |
| `GET` | `/api/v1/investigations/stats` | Get fraud statistics by action |
| `PUT` | `/api/v1/investigations/{id}/status` | Update investigation status |
| `POST` | `/api/v1/investigations/blacklist/{cardLast4}` | Add to blacklist |
| `DELETE` | `/api/v1/investigations/blacklist/{cardLast4}` | Remove from blacklist |
| `GET` | `/api/v1/risk/rules` | Get current rule configuration |
| `PUT` | `/api/v1/risk/rules/weights` | Update rule weights dynamically |

**Swagger UI:** `http://localhost:8083/swagger-ui.html`

---

### 4. Compliance Auditor — port 8082

Persistent audit storage. Consumes decisions.v1, persists to PostgreSQL. Full REST API for querying.

**Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/audit/decisions` | List all decisions with pagination |
| `GET` | `/api/v1/audit/decisions/{id}` | Get single decision |
| `GET` | `/api/v1/audit/decisions/search` | Search by card, IP, date range |
| `GET` | `/api/v1/audit/decisions/stats` | Statistics by action/type |
| `GET` | `/actuator/prometheus` | Prometheus metrics endpoint |

---

### 5. Alert Dispatcher — port 8084

High-risk alert dispatcher. Consumes fraud-alerts.v1 and dispatches to external channels (SMS, Email, Slack - configurable).

Consumes: `fraud-alerts.v1` (only decisions with score ≥ 0.9)

---

## Event Schema — `common/`

Every Kafka message follows this envelope:

```json
{
  "eventId": "uuid",
  "schemaVersion": "v1",
  "occurredAt": "2024-01-01T12:00:00Z",
  "transactionId": "uuid",
  "cardLast4": "1234",
  "correlationId": "uuid",
  "action": "ALLOW | FLAG | THROTTLE | BLOCK",
  "score": 0.75,
  "triggeredRules": [
    {"ruleName": "ip-velocity", "score": 0.6, "weight": 0.15, "details": "High IP velocity"}
  ],
  "reason": "ip-velocity=0.60, merchant-pattern=0.45",
  "expiresAt": "2024-01-01T13:00:00Z"
}
```

The same envelope is used for all decision events — consumers check `action`/`score` to decide what to do.

---

## Running Locally

```bash
docker compose -f deployments/docker-compose.yml up -d
```

Then in separate terminals:

```bash
mvn spring-boot:run -pl payment-ingestor
mvn spring-boot:run -pl fraud-analyzer
mvn spring-boot:run -pl compliance-auditor
mvn spring-boot:run -pl risk-orchestrator
mvn spring-boot:run -pl alert-dispatcher
```

| Service | URL |
|---------|-----|
| Payment Ingestor | http://localhost:8080 |
| Fraud Analyzer | http://localhost:8081 |
| Compliance Auditor | http://localhost:8082 |
| Risk Orchestrator | http://localhost:8083 |
| Alert Dispatcher | http://localhost:8084 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

---

## Testing the Flow

**1. Send a transaction → auto-analyzed → appears in audit:**

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"cardLast4":"1234","cardBin":"411111","amount":50.00,"currency":"USD","merchantId":"MERCH001","ipAddress":"192.168.1.1"}'
```

**2. Check cached decision:**

```bash
curl http://localhost:8083/api/v1/risk/decision?cardLast4=1234
# {"decision":"ALLOW","score":0.15,"reason":"Normal transaction","correlationId":"..."}
```

**3. Add card to blacklist:**

```bash
curl -X POST http://localhost:8083/api/v1/investigations/blacklist/9999
curl http://localhost:8083/api/v1/risk/decision?cardLast4=9999
# {"decision":"BLOCK","score":0.95,"reason":"Card 9999 is blacklisted"}
```

**4. Get statistics:**

```bash
curl http://localhost:8083/api/v1/investigations/stats
# [{"action":"ALLOW","count":120},{"action":"BLOCK","count":5}]
```

---

## Deployment (Kubernetes + ArgoCD)

### Kubernetes — `deployments/k8s/base/`

Manifests for all services, Kafka, Redis, PostgreSQL, and Grafana. Uses **Kustomize** to group resources.

- All services get health check probes (`/actuator/health`)
- ConfigMaps separate environment config from image builds
- Namespace: `fraud-detection`
- HorizontalPodAutoscalers for auto-scaling

### ArgoCD — `deployments/argocd-app.yaml`

- Watches `main` branch, path `deployments/k8s/base/`
- **Self-heal enabled** — any manual cluster changes are reverted automatically
- Every deployment is traceable to a git commit

### Helm Chart — `deployments/charts/fraud-detection/`

Alternative deployment method with `values.yaml` for dev/prod overlays.

---

## Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Backend language | Java 21 | Virtual threads, modern switch expressions |
| Backend framework | Spring Boot 3.3.x | Kafka, Redis, Resilience4j, Actuator out of the box |
| Message broker | Apache Kafka 3.7 | Durable, replayable, decoupled pub/sub |
| Cache | Redis 7.2 | Sub-millisecond lookups, sliding windows |
| Database | PostgreSQL 16 | ACID compliance for audit trails |
| Resilience | Resilience4j Circuit Breaker | Prevents cascading failures on DB/Redis outages |
| API docs | SpringDoc OpenAPI (Swagger) | Auto-generated from annotations |
| Containerization | Docker (multi-stage builds) | Lean images, reproducible builds |
| Local orchestration | Docker Compose | One-command startup for all services |
| Production orchestration | Kubernetes | Declarative, scalable, health-checked |
| GitOps / Auto-deploy | ArgoCD | Git as source of truth, automatic drift correction |
| Monitoring | Prometheus + Grafana | Built-in metrics, alerting rules |

---

## Testing

All tests pass (44 total):

```bash
mvn test
```

Test suites cover:
- Detection rules (velocity, blacklist, geolocation, time-based, amount, device, merchant, IP)
- Edge cases (null/empty values, concurrent transactions, boundary conditions)
- Controllers and services

---

## Roadmap

- **Redis-based rule configuration** — update thresholds without restart ✅
- **Circuit Breaker** — Resilience4j on all Redis/Kafka/PostgreSQL paths ✅
- **Prometheus Integration** — Micrometer metrics endpoint ✅
- **Kubernetes deployment** — manifests with health checks ✅
- **Helm chart** — parameterized deployment ✅
- **Alerting integration** — Slack/PagerDuty webhook receivers
- **SLA timers** — track MTTD and MTTR per merchant
- **Card fingerprinting** — extended device/card correlation
- **Multi-region deployment** — active-active Redis replication
- **ML model integration** — TensorFlow rule for anomaly detection