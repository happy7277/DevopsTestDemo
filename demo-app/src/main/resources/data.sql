START TRANSACTION;

INSERT IGNORE INTO demo_initialization (id) VALUES (1);
SET @seed_demo_users = ROW_COUNT();

INSERT IGNORE INTO users (id, name, email, status, created_at)
SELECT 1, 'Demo Ada', 'ada@example.com', 'ACTIVE', UTC_TIMESTAMP() - INTERVAL 1 DAY WHERE @seed_demo_users = 1
UNION ALL
SELECT 2, 'Demo Lin', 'lin@example.com', 'DISABLED', UTC_TIMESTAMP() - INTERVAL 2 DAY WHERE @seed_demo_users = 1
UNION ALL
SELECT 3, 'Demo Kai', 'kai@example.com', 'ACTIVE', UTC_TIMESTAMP() - INTERVAL 2 DAY WHERE @seed_demo_users = 1;

COMMIT;
