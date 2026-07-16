USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS order_comment (
    id bigint NOT NULL AUTO_INCREMENT,
    order_id bigint NOT NULL COMMENT 'order id',
    user_id bigint NOT NULL COMMENT 'user id',
    rider_id bigint NOT NULL COMMENT 'rider id',
    score tinyint NOT NULL COMMENT 'score: 1-5',
    content varchar(500) DEFAULT NULL COMMENT 'comment content',
    is_anonymous tinyint NOT NULL DEFAULT 0 COMMENT 'anonymous: 0 no, 1 yes',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_comment_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='order comment';
