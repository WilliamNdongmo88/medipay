CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL UNIQUE,

    balance NUMERIC(19, 2) DEFAULT 0,

    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_balance_positive CHECK (balance >= 0)
);

CREATE INDEX idx_wallets_last_updated ON wallets(last_updated);