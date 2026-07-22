-- V1.8 骑手申请与审核增量升级脚本。
-- 必须在 Navicat 中人工执行，禁止由应用自动执行。

SET @schema_name = DATABASE();

SET @reject_reason_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'rider_info'
      AND COLUMN_NAME = 'reject_reason'
);
SET @add_reject_reason = IF(
    @reject_reason_exists = 0,
    'ALTER TABLE rider_info ADD COLUMN reject_reason VARCHAR(255) NULL COMMENT ''审核拒绝或禁用原因'' AFTER rider_status',
    'SELECT 1'
);
PREPARE add_reject_reason_stmt FROM @add_reject_reason;
EXECUTE add_reject_reason_stmt;
DEALLOCATE PREPARE add_reject_reason_stmt;

SET @user_index_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'rider_info'
      AND INDEX_NAME = 'uk_rider_user_id'
);
SET @add_user_index = IF(
    @user_index_exists = 0,
    'ALTER TABLE rider_info ADD UNIQUE INDEX uk_rider_user_id (user_id)',
    'SELECT 1'
);
PREPARE add_user_index_stmt FROM @add_user_index;
EXECUTE add_user_index_stmt;
DEALLOCATE PREPARE add_user_index_stmt;
