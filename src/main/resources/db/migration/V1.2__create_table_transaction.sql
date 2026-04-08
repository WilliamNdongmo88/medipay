CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,

    sender_wallet_id BIGINT,
    receiver_wallet_id BIGINT,

    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),

    type VARCHAR(20),
    status VARCHAR(20),

    description TEXT,

    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (sender_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,
    FOREIGN KEY (receiver_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,

    CONSTRAINT chk_transaction_type
    CHECK (type IN ('DEPOSIT','PAYMENT','RESET')),

    CONSTRAINT chk_transaction_status
    CHECK (status IN ('PENDING','COMPLETED','FAILED')),

    CONSTRAINT chk_sender_receiver_diff
    CHECK (
        sender_wallet_id IS NULL
        OR receiver_wallet_id IS NULL
        OR sender_wallet_id <> receiver_wallet_id
    )
);

-- Index performance
CREATE INDEX idx_transactions_sender_wallet
ON transactions(sender_wallet_id);

CREATE INDEX idx_transactions_receiver_wallet
ON transactions(receiver_wallet_id);

CREATE INDEX idx_transactions_type
ON transactions(type);

CREATE INDEX idx_transactions_status
ON transactions(status);

CREATE INDEX idx_transactions_timestamp
ON transactions(timestamp DESC);