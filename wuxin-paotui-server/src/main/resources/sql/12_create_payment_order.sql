USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'payment record id',
    payment_no VARCHAR(64) NOT NULL COMMENT 'platform payment number and WeChat out_trade_no',
    order_id BIGINT NOT NULL COMMENT 'related order_info.id',
    order_no VARCHAR(64) NOT NULL COMMENT 'order number snapshot',
    user_id BIGINT NOT NULL COMMENT 'payer user id',
    payment_channel VARCHAR(20) NOT NULL COMMENT 'payment channel: MOCK or WECHAT',
    trade_type VARCHAR(20) NOT NULL COMMENT 'trade type: JSAPI',
    appid VARCHAR(32) DEFAULT NULL COMMENT 'WeChat mini program appid',
    mchid VARCHAR(32) DEFAULT NULL COMMENT 'WeChat merchant id',
    openid VARCHAR(128) DEFAULT NULL COMMENT 'payer openid; nullable only for local mock',
    amount_total INT UNSIGNED NOT NULL COMMENT 'payment amount in cents',
    currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT 'currency code',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 created, 1 waiting pay, 2 success, 3 closed, 4 failed',
    prepay_id VARCHAR(128) DEFAULT NULL COMMENT 'WeChat or mock prepay id',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT 'payment channel transaction id',
    payer_total INT UNSIGNED DEFAULT NULL COMMENT 'actual payer amount in cents',
    success_time DATETIME DEFAULT NULL COMMENT 'payment success time',
    expire_time DATETIME DEFAULT NULL COMMENT 'payment expiration time',
    notify_id VARCHAR(64) DEFAULT NULL COMMENT 'callback notification id',
    notify_body_hash CHAR(64) DEFAULT NULL COMMENT 'SHA-256 hash of callback raw body',
    error_code VARCHAR(64) DEFAULT NULL COMMENT 'last payment error code',
    error_message VARCHAR(512) DEFAULT NULL COMMENT 'last sanitized payment error message',
    version INT NOT NULL DEFAULT 0 COMMENT 'optimistic version',
    create_time DATETIME NOT NULL COMMENT 'create time',
    update_time DATETIME NOT NULL COMMENT 'update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    active_order_id BIGINT GENERATED ALWAYS AS (
        CASE
            WHEN deleted = 0 AND status IN (0, 1) THEN order_id
            ELSE NULL
        END
    ) STORED COMMENT 'active payment order constraint key',
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    UNIQUE KEY uk_transaction_id (transaction_id),
    UNIQUE KEY uk_notify_id (notify_id),
    UNIQUE KEY uk_active_order_id (active_order_id),
    KEY idx_payment_order_id (order_id, deleted, create_time),
    KEY idx_payment_user_id (user_id, deleted, create_time),
    KEY idx_payment_status_expire (status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='payment order record';
