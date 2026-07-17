USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS shopping_cart (
    id bigint NOT NULL AUTO_INCREMENT COMMENT 'shopping cart id',
    user_id bigint NOT NULL COMMENT 'related sys_user.id',
    store_id bigint NOT NULL COMMENT 'related merchant_store.id',
    product_id bigint NOT NULL COMMENT 'related merchant_product.id',
    quantity int NOT NULL DEFAULT 1 COMMENT 'product quantity',
    selected tinyint NOT NULL DEFAULT 1 COMMENT 'selected: 0 no, 1 yes',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_product_deleted (user_id, product_id, is_deleted),
    KEY idx_user_store_deleted (user_id, store_id, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user shopping cart';
