CREATE TYPE transaction_type_enum AS ENUM (
    'DEPOSIT',
    'PAYMENT',
    'RESET'
);

CREATE TYPE transaction_status_enum AS ENUM (
    'PENDING',
    'COMPLETED',
    'FAILED'
);