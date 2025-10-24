CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY,
  txn_id VARCHAR(128) NOT NULL UNIQUE,
  amount BIGINT NOT NULL,
  currency VARCHAR(8) NOT NULL,
  status VARCHAR(32) NOT NULL, -- PENDING, SUCCESS, FAILED, RETRYING, FAILED_PERMANENTLY
  provider VARCHAR(64),
  provider_txn_id VARCHAR(128),
  retry_count INT NOT NULL DEFAULT 0,
  max_retries INT NOT NULL DEFAULT 3,
  next_retry_at TIMESTAMPTZ,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_next_retry ON transactions(next_retry_at);
CREATE INDEX IF NOT EXISTS idx_transactions_txn_id ON transactions(txn_id);
