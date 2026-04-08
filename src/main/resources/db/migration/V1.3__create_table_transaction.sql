CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,

    sender_wallet_id BIGINT,
    receiver_wallet_id BIGINT,

    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),

    type transaction_type_enum,
    status transaction_status_enum,

    description TEXT,

    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (sender_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,
    FOREIGN KEY (receiver_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,

    CHECK (
        sender_wallet_id IS NULL
        OR receiver_wallet_id IS NULL
        OR sender_wallet_id <> receiver_wallet_id
    )
);

CREATE INDEX idx_transactions_wallet_timestamp ON transactions(sender_wallet_id, timestamp DESC);

CREATE INDEX idx_transactions_receiver_timestamp ON transactions(receiver_wallet_id, timestamp DESC);