CREATE TABLE qrcodes (
    id BIGSERIAL PRIMARY KEY,

    pharmacist_id BIGINT NOT NULL,

    code_value VARCHAR(100) NOT NULL UNIQUE,

    amount NUMERIC(19, 2),

    is_used BOOLEAN DEFAULT FALSE,

    expiry_date TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_qrcode_pharmacist
        FOREIGN KEY (pharmacist_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_qrcode_amount_positive
        CHECK (amount IS NULL OR amount >= 0)
);

-- Index
CREATE INDEX idx_qrcodes_pharmacist ON qrcodes(pharmacist_id);

CREATE INDEX idx_qrcodes_is_used ON qrcodes(is_used);

CREATE INDEX idx_qrcodes_expiry_date ON qrcodes(expiry_date);

CREATE INDEX idx_qrcodes_created_at ON qrcodes(created_at DESC);

CREATE INDEX idx_transactions_wallet_timestamp ON transactions(sender_wallet_id, timestamp DESC);

CREATE INDEX idx_transactions_receiver_timestamp ON transactions(receiver_wallet_id, timestamp DESC);

-- QR actifs uniquement
CREATE INDEX idx_qrcodes_active ON qrcodes(code_value) WHERE is_used = FALSE;