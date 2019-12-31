ALTER TABLE users
ADD is_exec_allowed TINYINT DEFAULT 0;

UPDATE users
SET is_exec_allowed = 1
WHERE is_enabled = 1;