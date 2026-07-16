USE wuxin_paotui;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN pay_status tinyint NOT NULL DEFAULT 0 COMMENT ''payment status: 0 unpaid, 1 paid'' AFTER finish_time',
        'SELECT ''order_info.pay_status column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'pay_status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN pay_time datetime DEFAULT NULL COMMENT ''payment time'' AFTER pay_status',
        'SELECT ''order_info.pay_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'pay_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN payment_no varchar(64) DEFAULT NULL COMMENT ''payment number'' AFTER pay_time',
        'SELECT ''order_info.payment_no column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'payment_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_pay_status_deleted_create_time (pay_status, deleted, create_time)',
        'SELECT ''idx_order_pay_status_deleted_create_time index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_pay_status_deleted_create_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD UNIQUE KEY uk_order_payment_no (payment_no)',
        'SELECT ''uk_order_payment_no index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'uk_order_payment_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
