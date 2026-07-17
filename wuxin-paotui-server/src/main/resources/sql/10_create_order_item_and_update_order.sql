USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS order_item (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'order item id',
    order_id bigint NOT NULL COMMENT 'related order_info.id',
    product_id bigint NOT NULL COMMENT 'related merchant_product.id',
    product_name varchar(100) NOT NULL COMMENT 'product name snapshot',
    product_image varchar(255) DEFAULT NULL COMMENT 'product image snapshot',
    product_price decimal(10,2) NOT NULL COMMENT 'product price snapshot',
    quantity int NOT NULL COMMENT 'product quantity',
    subtotal decimal(10,2) NOT NULL COMMENT 'product subtotal snapshot',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    KEY idx_order_item_order_deleted (order_id, is_deleted),
    KEY idx_order_item_product_deleted (product_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='product order item snapshot';

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_item ADD KEY idx_order_item_order_deleted (order_id, is_deleted)',
        'SELECT ''idx_order_item_order_deleted index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_item'
      AND INDEX_NAME = 'idx_order_item_order_deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_item ADD KEY idx_order_item_product_deleted (product_id, is_deleted)',
        'SELECT ''idx_order_item_product_deleted index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_item'
      AND INDEX_NAME = 'idx_order_item_product_deleted'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN order_type tinyint NOT NULL DEFAULT 0 COMMENT ''order type: 0 errand, 1 product''',
        'SELECT ''order_type column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'order_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN store_id bigint DEFAULT NULL COMMENT ''related merchant_store.id''',
        'SELECT ''store_id column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'store_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN product_amount decimal(10,2) DEFAULT NULL COMMENT ''product amount''',
        'SELECT ''product_amount column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'product_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN delivery_fee decimal(10,2) DEFAULT NULL COMMENT ''delivery fee''',
        'SELECT ''delivery_fee column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'delivery_fee'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN total_amount decimal(10,2) DEFAULT NULL COMMENT ''total payable amount''',
        'SELECT ''total_amount column already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'total_amount'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_type_user_deleted_create_time (order_type, user_id, deleted, create_time)',
        'SELECT ''idx_order_type_user_deleted_create_time index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_type_user_deleted_create_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_store_status_deleted_create_time (store_id, status, deleted, create_time)',
        'SELECT ''idx_order_store_status_deleted_create_time index already exists'''
    )
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'order_info'
      AND INDEX_NAME = 'idx_order_store_status_deleted_create_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
