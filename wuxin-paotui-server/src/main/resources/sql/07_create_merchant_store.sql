USE wuxin_paotui;

CREATE TABLE IF NOT EXISTS merchant_info (
    id bigint NOT NULL AUTO_INCREMENT,
    user_id bigint NOT NULL COMMENT 'related sys_user.id',
    merchant_name varchar(100) NOT NULL COMMENT 'merchant name',
    contact_name varchar(50) NOT NULL COMMENT 'contact name',
    contact_phone varchar(20) NOT NULL COMMENT 'contact phone',
    business_license varchar(255) DEFAULT NULL COMMENT 'business license image',
    id_card_front varchar(255) DEFAULT NULL COMMENT 'id card front image',
    id_card_back varchar(255) DEFAULT NULL COMMENT 'id card back image',
    audit_status tinyint NOT NULL DEFAULT 0 COMMENT 'audit status: 0 pending, 1 approved, 2 rejected',
    audit_remark varchar(255) DEFAULT NULL COMMENT 'audit remark',
    merchant_status tinyint NOT NULL DEFAULT 1 COMMENT 'merchant status: 0 disabled, 1 enabled',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchant_user_id (user_id),
    KEY idx_merchant_audit_status (audit_status, merchant_status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='merchant info';

CREATE TABLE IF NOT EXISTS merchant_store (
    id bigint NOT NULL AUTO_INCREMENT,
    merchant_id bigint NOT NULL COMMENT 'related merchant_info.id',
    store_name varchar(100) NOT NULL COMMENT 'store name',
    store_logo varchar(255) DEFAULT NULL COMMENT 'store logo',
    store_description varchar(500) DEFAULT NULL COMMENT 'store description',
    store_phone varchar(20) NOT NULL COMMENT 'store phone',
    province varchar(50) DEFAULT NULL COMMENT 'province',
    city varchar(50) DEFAULT NULL COMMENT 'city',
    district varchar(50) DEFAULT NULL COMMENT 'district',
    detail_address varchar(255) NOT NULL COMMENT 'detail address',
    latitude decimal(10,7) DEFAULT NULL COMMENT 'latitude',
    longitude decimal(10,7) DEFAULT NULL COMMENT 'longitude',
    business_status tinyint NOT NULL DEFAULT 0 COMMENT 'business status: 0 closed, 1 open',
    open_time time DEFAULT NULL COMMENT 'open time',
    close_time time DEFAULT NULL COMMENT 'close time',
    store_status tinyint NOT NULL DEFAULT 1 COMMENT 'store status: 0 disabled, 1 enabled',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted tinyint NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    UNIQUE KEY uk_store_merchant_id (merchant_id),
    KEY idx_store_business_status (business_status, store_status, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='merchant store';
