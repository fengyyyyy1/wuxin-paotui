USE wuxin_paotui;

-- Before execution, confirm that sys_role.role_code and
-- sys_user_role(user_id, role_id) contain no duplicate non-null values.

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'merchant_info'
          AND COLUMN_NAME = 'audit_admin_id'
    ) = 0,
    'ALTER TABLE merchant_info ADD COLUMN audit_admin_id bigint DEFAULT NULL COMMENT ''audit administrator user id'' AFTER audit_remark',
    'SELECT ''audit_admin_id column already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'merchant_info'
          AND COLUMN_NAME = 'audit_time'
    ) = 0,
    'ALTER TABLE merchant_info ADD COLUMN audit_time datetime DEFAULT NULL COMMENT ''audit time'' AFTER audit_admin_id',
    'SELECT ''audit_time column already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'merchant_info'
          AND COLUMN_NAME = 'reject_reason'
    ) = 0,
    'ALTER TABLE merchant_info ADD COLUMN reject_reason varchar(255) DEFAULT NULL COMMENT ''audit rejection reason'' AFTER audit_time',
    'SELECT ''reject_reason column already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'merchant_info'
          AND INDEX_NAME = 'idx_merchant_deleted_create_time'
    ) = 0,
    'ALTER TABLE merchant_info ADD KEY idx_merchant_deleted_create_time (is_deleted, create_time, id)',
    'SELECT ''idx_merchant_deleted_create_time index already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_role (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'role id',
    role_name varchar(50) DEFAULT NULL COMMENT 'role name',
    role_code varchar(50) DEFAULT NULL COMMENT 'role code',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='system role';

CREATE TABLE IF NOT EXISTS sys_user_role (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'user role id',
    user_id bigint DEFAULT NULL COMMENT 'related sys_user.id',
    role_id bigint DEFAULT NULL COMMENT 'related sys_role.id',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='system user role relation';

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sys_role'
          AND INDEX_NAME = 'uk_role_code'
    ) = 0,
    'ALTER TABLE sys_role ADD UNIQUE KEY uk_role_code (role_code)',
    'SELECT ''uk_role_code index already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (
        SELECT COUNT(*)
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sys_user_role'
          AND INDEX_NAME = 'uk_user_role'
    ) = 0,
    'ALTER TABLE sys_user_role ADD UNIQUE KEY uk_user_role (user_id, role_id)',
    'SELECT ''uk_user_role index already exists'''
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS merchant_audit_log (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'audit log id',
    merchant_id bigint NOT NULL COMMENT 'related merchant_info.id',
    admin_user_id bigint NOT NULL COMMENT 'administrator sys_user.id',
    action varchar(20) NOT NULL COMMENT 'APPROVE, REJECT, ENABLE or DISABLE',
    before_status tinyint NOT NULL COMMENT 'status before operation',
    after_status tinyint NOT NULL COMMENT 'status after operation',
    reason varchar(255) DEFAULT NULL COMMENT 'audit remark or operation reason',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'operation time',
    PRIMARY KEY (id),
    KEY idx_audit_log_merchant_time (merchant_id, create_time),
    KEY idx_audit_log_admin_time (admin_user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='merchant audit operation log';

INSERT INTO sys_role (
    role_name,
    role_code,
    create_time
) VALUES (
    '平台管理员',
    'ADMIN',
    CURRENT_TIMESTAMP
) ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name);

-- Administrator assignment is intentionally not automatic.
-- After confirming the real administrator account, execute the documented
-- INSERT ... SELECT statement in Navicat to grant the ADMIN role.
