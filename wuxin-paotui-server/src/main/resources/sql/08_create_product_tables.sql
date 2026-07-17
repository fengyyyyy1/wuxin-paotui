USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS merchant_category (
    id bigint NOT NULL AUTO_INCREMENT,
    store_id bigint NOT NULL COMMENT 'related merchant_store.id',
    category_name varchar(50) NOT NULL COMMENT 'category name',
    sort int NOT NULL DEFAULT 0 COMMENT 'sort ascending',
    status tinyint NOT NULL DEFAULT 1 COMMENT 'status: 0 disabled, 1 enabled',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_store_name (store_id, category_name),
    KEY idx_category_store_status_sort (store_id, status, is_deleted, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='merchant product category';

CREATE TABLE IF NOT EXISTS merchant_product (
    id bigint NOT NULL AUTO_INCREMENT,
    store_id bigint NOT NULL COMMENT 'related merchant_store.id',
    category_id bigint NOT NULL COMMENT 'related merchant_category.id',
    product_name varchar(100) NOT NULL COMMENT 'product name',
    product_image varchar(255) DEFAULT NULL COMMENT 'product image',
    product_description varchar(500) DEFAULT NULL COMMENT 'product description',
    price decimal(10,2) NOT NULL COMMENT 'sale price',
    original_price decimal(10,2) DEFAULT NULL COMMENT 'original price',
    stock int NOT NULL DEFAULT 0 COMMENT 'stock',
    sales int NOT NULL DEFAULT 0 COMMENT 'sales',
    product_status tinyint NOT NULL DEFAULT 0 COMMENT 'status: 0 off shelf, 1 on shelf',
    sort int NOT NULL DEFAULT 0 COMMENT 'sort ascending',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    KEY idx_product_store_status_sort (store_id, product_status, is_deleted, sort),
    KEY idx_product_category_status (category_id, product_status, is_deleted),
    KEY idx_product_store_name (store_id, product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='merchant product';
