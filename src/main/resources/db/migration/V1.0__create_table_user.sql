CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,

    role VARCHAR(20),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_role
    CHECK (role IN ('ROLE_ADMIN','ROLE_CLIENT','ROLE_PHARMACIST'))
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_created_at ON users(created_at);