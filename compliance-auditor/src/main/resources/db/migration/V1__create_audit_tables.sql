CREATE TABLE IF NOT EXISTS transaction_logs (
    id UUID PRIMARY KEY,
    event_id UUID,
    transaction_time TIMESTAMP WITH TIME ZONE,
    card_last4 VARCHAR(4),
    card_bin VARCHAR(6),
    amount DECIMAL(19,2),
    currency VARCHAR(3),
    merchant_id VARCHAR(100),
    merchant_mcc VARCHAR(10),
    ip_address VARCHAR(45),
    device_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_card_last4 ON transaction_logs(card_last4);
CREATE INDEX IF NOT EXISTS idx_transaction_time ON transaction_logs(transaction_time);

CREATE TABLE IF NOT EXISTS decision_logs (
    event_id UUID PRIMARY KEY,
    transaction_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE,
    card_last4 VARCHAR(4),
    action VARCHAR(20),
    score DECIMAL(5,4),
    reason VARCHAR(1000),
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_decision_card ON decision_logs(card_last4);
CREATE INDEX IF NOT EXISTS idx_decision_time ON decision_logs(occurred_at);

CREATE TABLE IF NOT EXISTS rule_logs (
    id SERIAL PRIMARY KEY,
    event_id UUID,
    rule_name VARCHAR(50),
    score DECIMAL(5,4),
    weight DECIMAL(5,4),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rule_event ON rule_logs(event_id);