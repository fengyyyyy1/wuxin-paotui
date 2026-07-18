USE wuxin_paotui;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN merchant_accept_time datetime DEFAULT NULL COMMENT ''merchant accept time'' AFTER finish_time',
        'SELECT ''merchant_accept_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'merchant_accept_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN merchant_ready_time datetime DEFAULT NULL COMMENT ''merchant ready time'' AFTER merchant_accept_time',
        'SELECT ''merchant_ready_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'merchant_ready_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN merchant_reject_time datetime DEFAULT NULL COMMENT ''merchant reject time'' AFTER merchant_ready_time',
        'SELECT ''merchant_reject_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'merchant_reject_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN merchant_reject_reason varchar(200) DEFAULT NULL COMMENT ''merchant reject reason'' AFTER merchant_reject_time',
        'SELECT ''merchant_reject_reason column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'merchant_reject_reason'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_store_type_deleted_create_time (store_id, order_type, deleted, create_time)',
        'SELECT ''idx_order_store_type_deleted_create_time index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_store_type_deleted_create_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
