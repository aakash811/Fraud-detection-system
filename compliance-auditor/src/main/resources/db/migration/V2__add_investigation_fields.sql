ALTER TABLE decision_logs ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(36);
ALTER TABLE decision_logs ADD COLUMN IF NOT EXISTS investigation_status VARCHAR(30) DEFAULT 'PENDING';
ALTER TABLE decision_logs ADD COLUMN IF NOT EXISTS investigator_notes VARCHAR(2000);
CREATE INDEX IF NOT EXISTS idx_decision_investigation ON decision_logs(investigation_status);