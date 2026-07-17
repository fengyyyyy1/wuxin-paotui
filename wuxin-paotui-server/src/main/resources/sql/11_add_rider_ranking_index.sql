USE wuxin_paotui;

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD KEY idx_order_status_deleted_finish_rider (status, deleted, finish_time, rider_id)',
        'SELECT ''rider ranking index already exists'''
    )
    FROM (
        SELECT INDEX_NAME
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = @schema_name
          AND TABLE_NAME = 'order_info'
        GROUP BY INDEX_NAME
        HAVING INDEX_NAME = 'idx_order_status_deleted_finish_rider'
            OR GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',')
               = 'status,deleted,finish_time,rider_id'
    ) AS matching_index
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
