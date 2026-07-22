-- V1.9 enterprise admin console incremental upgrade.
-- Execute manually in Navicat after backing up the database.
-- Do not run automatically from the application.

USE wuxin_paotui;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD COLUMN last_login_time DATETIME NULL COMMENT ''last successful login time'' AFTER update_time',
        'SELECT ''sys_user.last_login_time already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'last_login_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_user ADD COLUMN last_login_ip VARCHAR(64) NULL COMMENT ''last successful login ip'' AFTER last_login_time',
        'SELECT ''sys_user.last_login_ip already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'last_login_ip'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_role ADD COLUMN role_description VARCHAR(255) NULL COMMENT ''role description'' AFTER role_code',
        'SELECT ''sys_role.role_description already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_role'
      AND COLUMN_NAME = 'role_description'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_role ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT ''0 disabled, 1 enabled'' AFTER role_description',
        'SELECT ''sys_role.status already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_role'
      AND COLUMN_NAME = 'status'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE sys_role ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''update time'' AFTER create_time',
        'SELECT ''sys_role.update_time already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_role'
      AND COLUMN_NAME = 'update_time'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'permission id',
    permission_name VARCHAR(80) NOT NULL COMMENT 'permission display name',
    permission_code VARCHAR(80) NOT NULL COMMENT 'permission code',
    module_code VARCHAR(40) NOT NULL COMMENT 'admin module code',
    permission_type VARCHAR(20) NOT NULL DEFAULT 'API' COMMENT 'MENU, API or ACTION',
    sort INT NOT NULL DEFAULT 0 COMMENT 'display sort',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_permission_module_status (module_code, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='administrator permission';

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'role permission relation id',
    role_id BIGINT NOT NULL COMMENT 'sys_role.id',
    permission_id BIGINT NOT NULL COMMENT 'sys_permission.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_permission_permission (permission_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='role permission relation';

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'config id',
    config_group VARCHAR(40) NOT NULL COMMENT 'config group',
    config_key VARCHAR(100) NOT NULL COMMENT 'config key',
    config_value TEXT NULL COMMENT 'config value',
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT 'STRING, DECIMAL, INTEGER, BOOLEAN or TEXT',
    config_name VARCHAR(100) NOT NULL COMMENT 'display name',
    config_description VARCHAR(255) NULL COMMENT 'description',
    is_sensitive TINYINT NOT NULL DEFAULT 0 COMMENT '0 public value, 1 sensitive value',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    update_admin_id BIGINT NULL COMMENT 'last administrator sys_user.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_system_config_key (config_key),
    KEY idx_system_config_group_status (config_group, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='dynamic platform configuration';

CREATE TABLE IF NOT EXISTS banner (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'banner id',
    title VARCHAR(100) NOT NULL COMMENT 'banner title',
    subtitle VARCHAR(200) NULL COMMENT 'banner subtitle',
    image_url VARCHAR(500) NOT NULL COMMENT 'image url',
    target_type VARCHAR(30) NOT NULL DEFAULT 'NONE' COMMENT 'NONE, STORE, PRODUCT, PAGE or URL',
    target_value VARCHAR(500) NULL COMMENT 'target id, route or url',
    sort INT NOT NULL DEFAULT 0 COMMENT 'display sort',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    start_time DATETIME NULL COMMENT 'start time',
    end_time DATETIME NULL COMMENT 'end time',
    create_admin_id BIGINT NULL COMMENT 'creator sys_user.id',
    update_admin_id BIGINT NULL COMMENT 'last administrator sys_user.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    KEY idx_banner_status_time_sort (status, is_deleted, start_time, end_time, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='home banner';

CREATE TABLE IF NOT EXISTS notice (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'notice id',
    notice_type VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM, USER, MERCHANT or RIDER',
    title VARCHAR(120) NOT NULL COMMENT 'notice title',
    content TEXT NOT NULL COMMENT 'notice content',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 published, 2 offline',
    publish_time DATETIME NULL COMMENT 'publish time',
    expire_time DATETIME NULL COMMENT 'expire time',
    create_admin_id BIGINT NULL COMMENT 'creator sys_user.id',
    update_admin_id BIGINT NULL COMMENT 'last administrator sys_user.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'logic delete: 0 no, 1 yes',
    PRIMARY KEY (id),
    KEY idx_notice_type_status_time (notice_type, status, is_deleted, publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='platform notice';

CREATE TABLE IF NOT EXISTS home_recommendation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'recommendation id',
    recommendation_type VARCHAR(30) NOT NULL COMMENT 'STORE, PRODUCT, HOT_PRODUCT or CATEGORY',
    target_id BIGINT NOT NULL COMMENT 'business target id',
    title_override VARCHAR(100) NULL COMMENT 'optional display title',
    sort INT NOT NULL DEFAULT 0 COMMENT 'display sort',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    start_time DATETIME NULL COMMENT 'start time',
    end_time DATETIME NULL COMMENT 'end time',
    update_admin_id BIGINT NULL COMMENT 'last administrator sys_user.id',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_home_recommendation_target (recommendation_type, target_id),
    KEY idx_home_recommendation_status_sort (recommendation_type, status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='home recommendation configuration';

CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'operation log id',
    admin_user_id BIGINT NOT NULL COMMENT 'administrator sys_user.id',
    module_code VARCHAR(40) NOT NULL COMMENT 'module code',
    operation_code VARCHAR(80) NOT NULL COMMENT 'operation code',
    operation_name VARCHAR(120) NOT NULL COMMENT 'operation name',
    target_type VARCHAR(50) NULL COMMENT 'target type',
    target_id VARCHAR(80) NULL COMMENT 'target id',
    request_method VARCHAR(10) NULL COMMENT 'http method',
    request_path VARCHAR(255) NULL COMMENT 'request path',
    request_ip VARCHAR(64) NULL COMMENT 'request ip',
    before_data JSON NULL COMMENT 'before state snapshot',
    after_data JSON NULL COMMENT 'after state snapshot',
    result_status TINYINT NOT NULL DEFAULT 1 COMMENT '0 failed, 1 success',
    error_message VARCHAR(500) NULL COMMENT 'sanitized error message',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'operation time',
    PRIMARY KEY (id),
    KEY idx_admin_log_admin_time (admin_user_id, create_time),
    KEY idx_admin_log_module_time (module_code, create_time),
    KEY idx_admin_log_target (target_type, target_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='administrator operation log';

INSERT INTO sys_role (role_name, role_code, role_description, status, create_time)
VALUES
    ('超级管理员', 'SUPER_ADMIN', '拥有总控后台全部权限', 1, CURRENT_TIMESTAMP),
    ('运营管理员', 'OPERATIONS', '负责订单、商家、商品与运营配置', 1, CURRENT_TIMESTAMP),
    ('客服', 'CUSTOMER_SERVICE', '负责用户与订单服务', 1, CURRENT_TIMESTAMP),
    ('审核员', 'AUDITOR', '负责骑手和商家审核', 1, CURRENT_TIMESTAMP),
    ('财务', 'FINANCE', '负责财务数据查看', 1, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name),
    role_description = VALUES(role_description),
    status = VALUES(status);

INSERT INTO sys_permission
    (permission_name, permission_code, module_code, permission_type, sort, status)
VALUES
    ('查看Dashboard', 'dashboard:view', 'dashboard', 'MENU', 10, 1),
    ('查看订单', 'order:view', 'order', 'MENU', 20, 1),
    ('管理订单', 'order:manage', 'order', 'ACTION', 21, 1),
    ('查看用户', 'user:view', 'user', 'MENU', 30, 1),
    ('管理用户', 'user:manage', 'user', 'ACTION', 31, 1),
    ('查看骑手', 'rider:view', 'rider', 'MENU', 40, 1),
    ('审核骑手', 'rider:audit', 'rider', 'ACTION', 41, 1),
    ('管理骑手', 'rider:manage', 'rider', 'ACTION', 42, 1),
    ('查看商家', 'merchant:view', 'merchant', 'MENU', 50, 1),
    ('审核商家', 'merchant:audit', 'merchant', 'ACTION', 51, 1),
    ('管理商家', 'merchant:manage', 'merchant', 'ACTION', 52, 1),
    ('查看商品', 'product:view', 'product', 'MENU', 60, 1),
    ('管理商品', 'product:manage', 'product', 'ACTION', 61, 1),
    ('查看运营', 'operation:view', 'operation', 'MENU', 70, 1),
    ('管理运营', 'operation:manage', 'operation', 'ACTION', 71, 1),
    ('查看财务', 'finance:view', 'finance', 'MENU', 80, 1),
    ('查看系统配置', 'config:view', 'config', 'MENU', 90, 1),
    ('管理系统配置', 'config:manage', 'config', 'ACTION', 91, 1),
    ('查看权限', 'rbac:view', 'rbac', 'MENU', 100, 1),
    ('管理权限', 'rbac:manage', 'rbac', 'ACTION', 101, 1),
    ('查看日志', 'log:view', 'log', 'MENU', 110, 1)
ON DUPLICATE KEY UPDATE
    permission_name = VALUES(permission_name),
    module_code = VALUES(module_code),
    permission_type = VALUES(permission_type),
    sort = VALUES(sort),
    status = VALUES(status);

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.role_code IN ('ADMIN', 'SUPER_ADMIN');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
    'dashboard:view', 'order:view', 'order:manage', 'user:view',
    'rider:view', 'rider:manage', 'merchant:view', 'merchant:manage',
    'product:view', 'product:manage', 'operation:view', 'operation:manage',
    'config:view', 'config:manage', 'log:view'
)
WHERE r.role_code = 'OPERATIONS';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
    'dashboard:view', 'order:view', 'order:manage', 'user:view',
    'rider:view', 'merchant:view', 'product:view', 'log:view'
)
WHERE r.role_code = 'CUSTOMER_SERVICE';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
    'dashboard:view', 'rider:view', 'rider:audit',
    'merchant:view', 'merchant:audit', 'log:view'
)
WHERE r.role_code = 'AUDITOR';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (
    'dashboard:view', 'order:view', 'finance:view'
)
WHERE r.role_code = 'FINANCE';

INSERT INTO system_config
    (config_group, config_key, config_value, value_type, config_name, config_description, is_sensitive, status)
VALUES
    ('ERRAND', 'errand.base_delivery_fee', '5.00', 'DECIMAL', '基础配送费', '跑腿订单基础配送费用', 0, 1),
    ('ERRAND', 'errand.per_km_fee', '1.50', 'DECIMAL', '每公里费用', '超出基础距离后的每公里费用', 0, 1),
    ('ERRAND', 'errand.per_kg_fee', '1.00', 'DECIMAL', '每公斤费用', '超出基础重量后的每公斤费用', 0, 1),
    ('ERRAND', 'errand.night_surcharge', '2.00', 'DECIMAL', '夜间加价', '夜间服务固定加价', 0, 1),
    ('ERRAND', 'errand.holiday_surcharge_rate', '0.10', 'DECIMAL', '节假日加价比例', '0到1之间的小数', 0, 1),
    ('ERRAND', 'errand.weather_surcharge_rate', '0.10', 'DECIMAL', '恶劣天气加价比例', '0到1之间的小数', 0, 1),
    ('ERRAND', 'errand.minimum_order_amount', '0.00', 'DECIMAL', '起送金额', '订单最低金额', 0, 1),
    ('ERRAND', 'errand.max_delivery_distance_km', '30.00', 'DECIMAL', '最大配送距离', '单位公里', 0, 1),
    ('ERRAND', 'errand.cancel_rule', '待接单订单可取消，履约中订单需联系客服', 'TEXT', '取消订单规则', '用户端展示规则', 0, 1),
    ('ERRAND', 'errand.timeout_minutes', '60', 'INTEGER', '订单超时分钟数', '用于超时预警', 0, 1),
    ('ERRAND', 'errand.auto_complete_hours', '24', 'INTEGER', '自动完成小时数', '待确认订单自动完成时间', 0, 1),
    ('PLATFORM', 'platform.commission_rate', '0.05', 'DECIMAL', '平台抽成比例', '平台综合抽成比例', 0, 1),
    ('PLATFORM', 'platform.merchant_commission_rate', '0.05', 'DECIMAL', '商家抽成比例', '商品订单商家抽成比例', 0, 1),
    ('PLATFORM', 'platform.rider_reward_rate', '0.00', 'DECIMAL', '骑手奖励比例', '骑手奖励预留比例', 0, 1),
    ('USER', 'user.new_user_discount', '0.00', 'DECIMAL', '新人优惠', '新人首单优惠金额', 0, 1),
    ('USER', 'user.points_rate', '0.00', 'DECIMAL', '积分比例', '消费金额积分换算比例', 0, 1),
    ('USER', 'user.invite_reward', '0.00', 'DECIMAL', '邀请奖励', '邀请成功奖励金额', 0, 1),
    ('HOME', 'home.service_phone', '', 'STRING', '客服电话', '用户端客服电话', 0, 1),
    ('HOME', 'home.contact_us', '', 'TEXT', '联系我们', '平台联系方式', 0, 1),
    ('HOME', 'home.about_us', '', 'TEXT', '关于我们', '平台介绍', 0, 1),
    ('HOME', 'home.user_agreement', '', 'TEXT', '用户协议', '用户协议正文', 0, 1),
    ('HOME', 'home.privacy_policy', '', 'TEXT', '隐私政策', '隐私政策正文', 0, 1),
    ('SYSTEM', 'system.oss_config', '', 'TEXT', 'OSS配置', '预留，生产环境使用密钥管理服务', 1, 0),
    ('SYSTEM', 'system.wechat_pay_config', '', 'TEXT', '微信支付配置', '预留，生产环境使用环境变量', 1, 0),
    ('SYSTEM', 'system.wechat_app_id', '', 'STRING', '微信AppID', '预留配置引用', 0, 0),
    ('SYSTEM', 'system.wechat_app_secret', '', 'STRING', '微信AppSecret', '预留，禁止明文返回', 1, 0),
    ('SYSTEM', 'system.sms_config', '', 'TEXT', '短信配置', '预留，生产环境使用密钥管理服务', 1, 0)
ON DUPLICATE KEY UPDATE
    config_group = VALUES(config_group),
    value_type = VALUES(value_type),
    config_name = VALUES(config_name),
    config_description = VALUES(config_description),
    is_sensitive = VALUES(is_sensitive);
