CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_users_created (created_at)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor VARCHAR(254) NOT NULL,
    action VARCHAR(32) NOT NULL,
    target_id BIGINT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS demo_initialization (
    id INT PRIMARY KEY
);
