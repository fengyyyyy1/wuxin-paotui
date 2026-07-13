USE wuxin_paotui;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN accept_time datetime DEFAULT NULL COMMENT ''accept time'' AFTER update_time',
        'SELECT ''order_info.accept_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'accept_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN finish_time datetime DEFAULT NULL COMMENT ''finish time'' AFTER accept_time',
        'SELECT ''order_info.finish_time column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'finish_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_rider_status_deleted (rider_id, status, deleted)',
        'SELECT ''idx_order_rider_status_deleted index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_rider_status_deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_status_deleted_create_time (status, deleted, create_time)',
        'SELECT ''idx_order_status_deleted_create_time index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_status_deleted_create_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
