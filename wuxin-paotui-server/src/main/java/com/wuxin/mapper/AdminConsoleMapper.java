package com.wuxin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AdminConsoleMapper {

    @Select("""
            SELECT
              (SELECT COUNT(*) FROM order_info WHERE deleted = 0 AND create_time >= CURRENT_DATE) AS todayOrders,
              (SELECT COALESCE(SUM(COALESCE(total_amount, price, 0)), 0) FROM order_info
               WHERE deleted = 0 AND pay_status = 1 AND pay_time >= CURRENT_DATE) AS todayRevenue,
              (SELECT COUNT(*) FROM order_info WHERE deleted = 0 AND finish_time >= CURRENT_DATE) AS todayDeliveries,
              (SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 AND create_time >= CURRENT_DATE) AS newUsers,
              (SELECT COUNT(*) FROM rider_info WHERE create_time >= CURRENT_DATE) AS newRiders,
              (SELECT COUNT(*) FROM merchant_info WHERE is_deleted = 0 AND create_time >= CURRENT_DATE) AS newMerchants,
              (SELECT COUNT(*) FROM rider_info WHERE audit_status = 0) AS pendingRiders,
              (SELECT COUNT(*) FROM merchant_info WHERE is_deleted = 0 AND audit_status = 0) AS pendingMerchants,
              (SELECT COUNT(*) FROM order_info WHERE deleted = 0 AND status = 8) AS pendingRefunds
            """)
    AdminConsoleVO.Dashboard selectDashboardSummary();

    @Select("""
            SELECT DATE(create_time) AS date, CAST(COUNT(*) AS DECIMAL(18,2)) AS value
            FROM order_info
            WHERE deleted = 0 AND create_time >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
            GROUP BY DATE(create_time)
            ORDER BY date
            """)
    List<AdminConsoleVO.TrendPoint> selectOrderTrend();

    @Select("""
            SELECT DATE(pay_time) AS date, COALESCE(SUM(COALESCE(total_amount, price, 0)), 0) AS value
            FROM order_info
            WHERE deleted = 0 AND pay_status = 1
              AND pay_time >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
            GROUP BY DATE(pay_time)
            ORDER BY date
            """)
    List<AdminConsoleVO.TrendPoint> selectRevenueTrend();

    @Select("""
            SELECT oi.product_id AS id, MAX(oi.product_name) AS name,
                   COALESCE(SUM(oi.subtotal), 0) AS value, COALESCE(SUM(oi.quantity), 0) AS count
            FROM order_item oi
            JOIN order_info o ON o.id = oi.order_id AND o.deleted = 0 AND o.pay_status = 1
            WHERE oi.is_deleted = 0
            GROUP BY oi.product_id
            ORDER BY count DESC, value DESC
            LIMIT 10
            """)
    List<AdminConsoleVO.RankingItem> selectTopProducts();

    @Select("""
            SELECT s.id, s.store_name AS name,
                   COALESCE(SUM(COALESCE(o.total_amount, o.price, 0)), 0) AS value,
                   COUNT(o.id) AS count
            FROM merchant_store s
            JOIN order_info o ON o.store_id = s.id AND o.deleted = 0 AND o.pay_status = 1
            WHERE s.is_deleted = 0
            GROUP BY s.id, s.store_name
            ORDER BY value DESC, count DESC
            LIMIT 10
            """)
    List<AdminConsoleVO.RankingItem> selectTopMerchants();

    @Select("""
            SELECT r.id, COALESCE(r.real_name, u.nickname, u.username) AS name,
                   CAST(COUNT(o.id) AS DECIMAL(18,2)) AS value, COUNT(o.id) AS count
            FROM rider_info r
            LEFT JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0
            JOIN order_info o ON o.rider_id = r.id AND o.deleted = 0 AND o.status = 4
            GROUP BY r.id, r.real_name, u.nickname, u.username
            ORDER BY count DESC
            LIMIT 10
            """)
    List<AdminConsoleVO.RankingItem> selectTopRiders();

    @Select(ORDER_SELECT + " ORDER BY o.create_time DESC, o.id DESC LIMIT 10")
    List<AdminConsoleVO.OrderRow> selectRecentOrders();

    String ORDER_SELECT = """
            SELECT o.id AS orderId, o.order_no AS orderNo, o.order_type AS orderType,
                   o.user_id AS userId, COALESCE(u.nickname, u.username) AS userName,
                   o.rider_id AS riderId, COALESCE(ru.nickname, r.real_name, ru.username) AS riderName,
                   m.id AS merchantId, o.store_id AS storeId, s.store_name AS storeName,
                   COALESCE(o.goods_name, (SELECT MAX(oi.product_name) FROM order_item oi
                       WHERE oi.order_id = o.id AND oi.is_deleted = 0)) AS goodsName,
                   COALESCE(o.total_amount, o.price, 0) AS totalAmount,
                   o.pay_status AS payStatus, o.status, o.create_time AS createTime,
                   o.finish_time AS finishTime,
                   CASE WHEN o.status = 8 OR (o.status NOT IN (4, 5) AND o.create_time &lt; DATE_SUB(NOW(), INTERVAL 2 HOUR))
                        THEN TRUE ELSE FALSE END AS abnormal
            FROM order_info o
            LEFT JOIN sys_user u ON u.id = o.user_id
            LEFT JOIN rider_info r ON r.id = o.rider_id
            LEFT JOIN sys_user ru ON ru.id = r.user_id
            LEFT JOIN merchant_store s ON s.id = o.store_id AND s.is_deleted = 0
            LEFT JOIN merchant_info m ON m.id = s.merchant_id AND m.is_deleted = 0
            WHERE o.deleted = 0
            """;

    @Select({
            "<script>", ORDER_SELECT,
            "<if test='orderType != null'>AND o.order_type = #{orderType}</if>",
            "<if test='status != null'>AND o.status = #{status}</if>",
            "<if test='userId != null'>AND o.user_id = #{userId}</if>",
            "<if test='riderId != null'>AND o.rider_id = #{riderId}</if>",
            "<if test='merchantId != null'>AND m.id = #{merchantId}</if>",
            "<if test='startTime != null'>AND o.create_time &gt;= #{startTime}</if>",
            "<if test='endTime != null'>AND o.create_time &lt;= #{endTime}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.phone LIKE CONCAT('%', #{keyword}, '%') OR s.store_name LIKE CONCAT('%', #{keyword}, '%'))</if>",
            "<if test='abnormalOnly != null and abnormalOnly'>AND (o.status = 8 OR (o.status NOT IN (4, 5) AND o.create_time &lt; DATE_SUB(NOW(), INTERVAL 2 HOUR)))</if>",
            "ORDER BY o.create_time DESC, o.id DESC",
            "</script>"
    })
    Page<AdminConsoleVO.OrderRow> selectOrderPage(
            Page<AdminConsoleVO.OrderRow> page,
            @Param("orderType") Integer orderType,
            @Param("status") Integer status,
            @Param("userId") Long userId,
            @Param("riderId") Long riderId,
            @Param("merchantId") Long merchantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword,
            @Param("abnormalOnly") Boolean abnormalOnly);

    @Select(ORDER_SELECT + " AND o.id = #{orderId} LIMIT 1")
    AdminConsoleVO.OrderDetail selectOrderDetail(@Param("orderId") Long orderId);

    @Select("""
            SELECT product_id AS productId, product_name AS productName, product_image AS productImage,
                   product_price AS productPrice, quantity, subtotal
            FROM order_item WHERE order_id = #{orderId} AND is_deleted = 0 ORDER BY id
            """)
    List<AdminConsoleVO.OrderItem> selectOrderItems(@Param("orderId") Long orderId);

    @Select("""
            SELECT id, old_status AS oldStatus, new_status AS newStatus, operator_id AS operatorId,
                   operator_type AS operatorType, remark, create_time AS createTime
            FROM order_log WHERE order_id = #{orderId} ORDER BY create_time, id
            """)
    List<AdminConsoleVO.OrderLog> selectOrderLogs(@Param("orderId") Long orderId);

    @Select({
            "<script>",
            "SELECT u.id AS userId, u.username, u.nickname, u.avatar, u.phone, u.status,",
            "COUNT(o.id) AS orderCount, COALESCE(SUM(CASE WHEN o.pay_status = 1 THEN COALESCE(o.total_amount, o.price, 0) ELSE 0 END), 0) AS consumptionAmount,",
            "u.last_login_time AS lastLoginTime, u.create_time AS createTime",
            "FROM sys_user u LEFT JOIN order_info o ON o.user_id = u.id AND o.deleted = 0",
            "WHERE u.is_deleted = 0",
            "<if test='status != null'>AND u.status = #{status}</if>",
            "<if test='startTime != null'>AND u.create_time &gt;= #{startTime}</if>",
            "<if test='endTime != null'>AND u.create_time &lt;= #{endTime}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.phone LIKE CONCAT('%', #{keyword}, '%'))</if>",
            "GROUP BY u.id, u.username, u.nickname, u.avatar, u.phone, u.status, u.last_login_time, u.create_time",
            "ORDER BY u.create_time DESC, u.id DESC",
            "</script>"
    })
    Page<AdminConsoleVO.UserRow> selectUserPage(
            Page<AdminConsoleVO.UserRow> page,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT r.id AS riderId, r.user_id AS userId, u.username, u.nickname, u.phone,",
            "r.real_name AS realName, r.id_card AS idCard, r.id_card_front AS idCardFront, r.id_card_back AS idCardBack,",
            "r.audit_status AS auditStatus, r.rider_status AS riderStatus, r.reject_reason AS rejectReason,",
            "COUNT(o.id) AS deliveryCount, SUM(CASE WHEN o.status = 4 THEN 1 ELSE 0 END) AS completedCount,",
            "CASE WHEN COUNT(o.id) = 0 THEN 0 ELSE ROUND(SUM(CASE WHEN o.status = 4 THEN 1 ELSE 0 END) / COUNT(o.id) * 100, 2) END AS completionRate,",
            "r.create_time AS createTime, r.update_time AS updateTime",
            "FROM rider_info r LEFT JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0",
            "LEFT JOIN order_info o ON o.rider_id = r.id AND o.deleted = 0",
            "WHERE 1 = 1",
            "<if test='auditStatus != null'>AND r.audit_status = #{auditStatus}</if>",
            "<if test='riderStatus != null'>AND r.rider_status = #{riderStatus}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (r.real_name LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.phone LIKE CONCAT('%', #{keyword}, '%'))</if>",
            "GROUP BY r.id, r.user_id, u.username, u.nickname, u.phone, r.real_name, r.id_card, r.id_card_front, r.id_card_back, r.audit_status, r.rider_status, r.reject_reason, r.create_time, r.update_time",
            "ORDER BY r.create_time DESC, r.id DESC",
            "</script>"
    })
    Page<AdminConsoleVO.RiderRow> selectRiderPage(
            Page<AdminConsoleVO.RiderRow> page,
            @Param("auditStatus") Integer auditStatus,
            @Param("riderStatus") Integer riderStatus,
            @Param("keyword") String keyword);

    @Select("""
            SELECT r.id AS riderId, r.user_id AS userId, u.username, u.nickname, u.phone,
                   r.real_name AS realName, r.id_card AS idCard, r.id_card_front AS idCardFront,
                   r.id_card_back AS idCardBack, r.audit_status AS auditStatus,
                   r.rider_status AS riderStatus, r.reject_reason AS rejectReason,
                   COUNT(o.id) AS deliveryCount,
                   SUM(CASE WHEN o.status = 4 THEN 1 ELSE 0 END) AS completedCount,
                   CASE WHEN COUNT(o.id) = 0 THEN 0 ELSE ROUND(SUM(CASE WHEN o.status = 4 THEN 1 ELSE 0 END) / COUNT(o.id) * 100, 2) END AS completionRate,
                   r.create_time AS createTime, r.update_time AS updateTime
            FROM rider_info r
            LEFT JOIN sys_user u ON u.id = r.user_id AND u.is_deleted = 0
            LEFT JOIN order_info o ON o.rider_id = r.id AND o.deleted = 0
            WHERE r.id = #{riderId}
            GROUP BY r.id, r.user_id, u.username, u.nickname, u.phone, r.real_name, r.id_card,
                     r.id_card_front, r.id_card_back, r.audit_status, r.rider_status,
                     r.reject_reason, r.create_time, r.update_time
            """)
    AdminConsoleVO.RiderRow selectRiderDetail(@Param("riderId") Long riderId);

    @Select({
            "<script>",
            "SELECT p.id AS productId, p.store_id AS storeId, s.store_name AS storeName, s.merchant_id AS merchantId,",
            "p.category_id AS categoryId, c.category_name AS categoryName, p.product_name AS productName,",
            "p.product_image AS productImage, p.price, p.stock, p.sales, p.product_status AS productStatus,",
            "EXISTS(SELECT 1 FROM home_recommendation hr WHERE hr.recommendation_type = 'PRODUCT' AND hr.target_id = p.id AND hr.status = 1) AS recommended,",
            "EXISTS(SELECT 1 FROM home_recommendation hr WHERE hr.recommendation_type = 'HOT_PRODUCT' AND hr.target_id = p.id AND hr.status = 1) AS hot,",
            "p.update_time AS updateTime",
            "FROM merchant_product p JOIN merchant_store s ON s.id = p.store_id AND s.is_deleted = 0",
            "LEFT JOIN merchant_category c ON c.id = p.category_id AND c.is_deleted = 0",
            "WHERE p.is_deleted = 0",
            "<if test='storeId != null'>AND p.store_id = #{storeId}</if>",
            "<if test='categoryId != null'>AND p.category_id = #{categoryId}</if>",
            "<if test='productStatus != null'>AND p.product_status = #{productStatus}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (p.product_name LIKE CONCAT('%', #{keyword}, '%') OR s.store_name LIKE CONCAT('%', #{keyword}, '%'))</if>",
            "<if test='recommended != null and recommended'>AND EXISTS(SELECT 1 FROM home_recommendation hr WHERE hr.recommendation_type = 'PRODUCT' AND hr.target_id = p.id AND hr.status = 1)</if>",
            "<if test='hot != null and hot'>AND EXISTS(SELECT 1 FROM home_recommendation hr WHERE hr.recommendation_type = 'HOT_PRODUCT' AND hr.target_id = p.id AND hr.status = 1)</if>",
            "ORDER BY p.update_time DESC, p.id DESC",
            "</script>"
    })
    Page<AdminConsoleVO.ProductRow> selectProductPage(
            Page<AdminConsoleVO.ProductRow> page,
            @Param("storeId") Long storeId,
            @Param("categoryId") Long categoryId,
            @Param("productStatus") Integer productStatus,
            @Param("keyword") String keyword,
            @Param("recommended") Boolean recommended,
            @Param("hot") Boolean hot);

    @Select("""
            SELECT p.id AS productId, p.store_id AS storeId, s.store_name AS storeName,
                   s.merchant_id AS merchantId, p.category_id AS categoryId,
                   c.category_name AS categoryName, p.product_name AS productName,
                   p.product_image AS productImage, p.price, p.stock, p.sales,
                   p.product_status AS productStatus,
                   EXISTS(SELECT 1 FROM home_recommendation hr
                          WHERE hr.recommendation_type = 'PRODUCT'
                            AND hr.target_id = p.id AND hr.status = 1) AS recommended,
                   EXISTS(SELECT 1 FROM home_recommendation hr
                          WHERE hr.recommendation_type = 'HOT_PRODUCT'
                            AND hr.target_id = p.id AND hr.status = 1) AS hot,
                   p.update_time AS updateTime
            FROM merchant_product p
            JOIN merchant_store s ON s.id = p.store_id AND s.is_deleted = 0
            LEFT JOIN merchant_category c ON c.id = p.category_id AND c.is_deleted = 0
            WHERE p.id = #{productId} AND p.is_deleted = 0
            LIMIT 1
            """)
    AdminConsoleVO.ProductRow selectProductById(@Param("productId") Long productId);

    @Select("""
            SELECT c.id AS categoryId, c.store_id AS storeId, s.store_name AS storeName,
                   c.category_name AS categoryName, c.status, c.sort, COUNT(p.id) AS productCount
            FROM merchant_category c
            JOIN merchant_store s ON s.id = c.store_id AND s.is_deleted = 0
            LEFT JOIN merchant_product p ON p.category_id = c.id AND p.is_deleted = 0
            WHERE c.is_deleted = 0
            GROUP BY c.id, c.store_id, s.store_name, c.category_name, c.status, c.sort
            ORDER BY s.store_name, c.sort, c.id
            """)
    List<AdminConsoleVO.CategoryRow> selectCategories();

    @Select("""
            SELECT
              COALESCE(SUM(CASE WHEN pay_status = 1 THEN COALESCE(total_amount, price, 0) ELSE 0 END), 0) AS orderAmount,
              COALESCE(SUM(CASE WHEN pay_status = 1 AND pay_time >= CURRENT_DATE THEN COALESCE(total_amount, price, 0) ELSE 0 END), 0) AS todayIncome,
              COALESCE(SUM(CASE WHEN pay_status = 1 AND pay_time >= DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY) AND pay_time &lt; CURRENT_DATE THEN COALESCE(total_amount, price, 0) ELSE 0 END), 0) AS yesterdayIncome,
              COALESCE(SUM(CASE WHEN pay_status = 1 AND pay_time >= DATE_FORMAT(CURRENT_DATE, '%Y-%m-01') THEN COALESCE(total_amount, price, 0) ELSE 0 END), 0) AS monthIncome
            FROM order_info WHERE deleted = 0
            """)
    AdminConsoleVO.Finance selectFinanceBase();

    @Select("""
            SELECT r.id AS roleId, r.role_name AS roleName, r.role_code AS roleCode,
                   r.role_description AS roleDescription, r.status
            FROM sys_role r ORDER BY r.id
            """)
    List<AdminConsoleVO.Role> selectRoles();

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId} ORDER BY permission_id")
    List<Long> selectRolePermissionIds(@Param("roleId") Long roleId);

    @Select("""
            SELECT id AS permissionId, permission_name AS permissionName,
                   permission_code AS permissionCode, module_code AS moduleCode,
                   permission_type AS permissionType, sort, status
            FROM sys_permission ORDER BY sort, id
            """)
    List<AdminConsoleVO.Permission> selectPermissions();

    @Select("""
            SELECT DISTINCT u.id AS userId, u.username, u.nickname, u.phone, u.status,
                            u.last_login_time AS lastLoginTime
            FROM sys_user u
            JOIN sys_user_role ur ON ur.user_id = u.id
            JOIN sys_role r ON r.id = ur.role_id
            WHERE u.is_deleted = 0
            ORDER BY u.id
            """)
    List<AdminConsoleVO.AdminUser> selectAdminUsers();

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId} ORDER BY role_id")
    List<Long> selectUserRoleIds(@Param("userId") Long userId);

    @Select("""
            SELECT r.role_name
            FROM sys_user_role ur JOIN sys_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId} ORDER BY r.id
            """)
    List<String> selectUserRoleNames(@Param("userId") Long userId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);

    @Insert({
            "<script>",
            "INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES",
            "<foreach collection='roleIds' item='roleId' separator=','>(#{userId}, #{roleId}, NOW())</foreach>",
            "</script>"
    })
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);

    @Insert({
            "<script>",
            "INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES",
            "<foreach collection='permissionIds' item='permissionId' separator=','>(#{roleId}, #{permissionId}, NOW())</foreach>",
            "</script>"
    })
    int insertRolePermissions(
            @Param("roleId") Long roleId,
            @Param("permissionIds") List<Long> permissionIds);

    @Select({
            "<script>",
            "SELECT l.id, l.admin_user_id AS adminUserId, u.username AS adminUsername,",
            "l.module_code AS moduleCode, l.operation_code AS operationCode, l.operation_name AS operationName,",
            "l.target_type AS targetType, l.target_id AS targetId, l.request_method AS requestMethod,",
            "l.request_path AS requestPath, l.request_ip AS requestIp, CAST(l.before_data AS CHAR) AS beforeData,",
            "CAST(l.after_data AS CHAR) AS afterData, l.result_status AS resultStatus, l.error_message AS errorMessage,",
            "l.create_time AS createTime",
            "FROM admin_operation_log l LEFT JOIN sys_user u ON u.id = l.admin_user_id",
            "WHERE 1 = 1",
            "<if test='adminUserId != null'>AND l.admin_user_id = #{adminUserId}</if>",
            "<if test='moduleCode != null and moduleCode != &quot;&quot;'>AND l.module_code = #{moduleCode}</if>",
            "<if test='resultStatus != null'>AND l.result_status = #{resultStatus}</if>",
            "<if test='startTime != null'>AND l.create_time &gt;= #{startTime}</if>",
            "<if test='endTime != null'>AND l.create_time &lt;= #{endTime}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>AND (l.operation_name LIKE CONCAT('%', #{keyword}, '%') OR l.target_id LIKE CONCAT('%', #{keyword}, '%') OR u.username LIKE CONCAT('%', #{keyword}, '%'))</if>",
            "ORDER BY l.create_time DESC, l.id DESC",
            "</script>"
    })
    Page<AdminConsoleVO.OperationLog> selectOperationLogPage(
            Page<AdminConsoleVO.OperationLog> page,
            @Param("adminUserId") Long adminUserId,
            @Param("moduleCode") String moduleCode,
            @Param("resultStatus") Integer resultStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword);
}
