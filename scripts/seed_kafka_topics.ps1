$ErrorActionPreference = "Stop"

$composeFile = "deployments/docker-compose.yml"
$topics = @("transactions.v1", "decisions.v1", "fraud-alerts.v1")

foreach ($topic in $topics) {
    docker compose -f $composeFile exec -T kafka kafka-topics `
        --create `
        --topic $topic `
        --bootstrap-server localhost:9092 `
        --partitions 6 `
        --replication-factor 1 `
        --if-not-exists

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create Kafka topic: $topic"
    }
}

Write-Host "Kafka topics created successfully"
