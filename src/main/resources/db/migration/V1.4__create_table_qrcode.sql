CREATE TABLE qrcodes (
    id BIGSERIAL PRIMARY KEY,

    pharmacist_id BIGINT NOT NULL,

    code_value VARCHAR(100) NOT NULL,

    amount NUMERIC(19, 2),

    is_used BOOLEAN DEFAULT FALSE,

    expiry_date TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Clé étrangère
    CONSTRAINT fk_qrcode_pharmacist
        FOREIGN KEY (pharmacist_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    -- Contraintes
    CONSTRAINT uk_qrcode_code UNIQUE (code_value),

    CONSTRAINT chk_qrcode_amount_positive
        CHECK (amount IS NULL OR amount >= 0)
);

-- Recherche par pharmacien
CREATE INDEX idx_qrcodes_pharmacist ON qrcodes(pharmacist_id);

-- Filtrage par statut (utilisé / non utilisé)
CREATE INDEX idx_qrcodes_is_used ON qrcodes(is_used);

-- Gestion expiration
CREATE INDEX idx_qrcodes_expiry_date ON qrcodes(expiry_date);

-- Tri historique
CREATE INDEX idx_qrcodes_created_at ON qrcodes(created_at DESC);