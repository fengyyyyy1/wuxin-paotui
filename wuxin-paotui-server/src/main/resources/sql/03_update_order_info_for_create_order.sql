USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS order_info (
    id bigint NOT NULL AUTO_INCREMENT,
    order_no varchar(50) NOT NULL COMMENT 'order number',
    user_id bigint NOT NULL COMMENT 'user id',
    rider_id bigint DEFAULT NULL COMMENT 'rider id',
    pickup_address_id bigint DEFAULT NULL COMMENT 'pickup address id',
    delivery_address_id bigint DEFAULT NULL COMMENT 'delivery address id',
    goods_name varchar(100) DEFAULT NULL COMMENT 'goods name',
    goods_description varchar(500) DEFAULT NULL COMMENT 'goods description',
    weight decimal(10,2) DEFAULT NULL COMMENT 'goods weight',
    distance decimal(10,2) DEFAULT NULL COMMENT 'delivery distance',
    price decimal(10,2) DEFAULT NULL COMMENT 'order price',
    status int NOT NULL DEFAULT 0 COMMENT 'order status: 0 waiting accept',
    remark varchar(500) DEFAULT NULL COMMENT 'remark',
    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='order info';

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN pickup_address_id bigint DEFAULT NULL COMMENT ''pickup address id'' AFTER rider_id',
        'SELECT ''pickup_address_id column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'pickup_address_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN delivery_address_id bigint DEFAULT NULL COMMENT ''delivery address id'' AFTER pickup_address_id',
        'SELECT ''delivery_address_id column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'delivery_address_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN goods_name varchar(100) DEFAULT NULL COMMENT ''goods name'' AFTER delivery_address_id',
        'SELECT ''goods_name column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'goods_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN goods_description varchar(500) DEFAULT NULL COMMENT ''goods description'' AFTER goods_name',
        'SELECT ''goods_description column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'goods_description'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN weight decimal(10,2) DEFAULT NULL COMMENT ''goods weight'' AFTER goods_description',
        'SELECT ''weight column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'weight'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN price decimal(10,2) DEFAULT NULL COMMENT ''order price'' AFTER distance',
        'SELECT ''price column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'price'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 COMMENT ''logic delete: 0 no, 1 yes'' AFTER update_time',
        'SELECT ''deleted column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE order_info
SET status = '0'
WHERE status IS NULL
   OR status NOT REGEXP '^-?[0-9]+$';

ALTER TABLE order_info
    MODIFY COLUMN status int NOT NULL DEFAULT 0 COMMENT 'order status: 0 waiting accept';

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD UNIQUE KEY uk_order_no (order_no)',
        'SELECT ''uk_order_no index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'uk_order_no'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
