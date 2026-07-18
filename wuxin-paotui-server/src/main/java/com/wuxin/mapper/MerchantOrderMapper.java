package com.wuxin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.vo.MerchantOrderDetailVO;
import com.wuxin.vo.MerchantOrderPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface MerchantOrderMapper {

    @Select({
            "<script>",
            "SELECT o.id AS orderId, o.order_no AS orderNo, o.status,",
            "o.pay_status AS payStatus, o.product_amount AS productAmount,",
            "o.delivery_fee AS deliveryFee, o.total_amount AS totalAmount,",
            "(SELECT GROUP_CONCAT(CONCAT(oi.product_name, ' x', oi.quantity)",
            "ORDER BY oi.id SEPARATOR '、') FROM order_item oi",
            "WHERE oi.order_id = o.id AND oi.is_deleted = 0) AS goodsSummary,",
            "a.receiver_name AS receiverName, a.receiver_phone AS receiverPhone,",
            "CONCAT_WS('', a.province, a.city, a.district, a.detail_address) AS deliveryAddress,",
            "o.create_time AS createTime, o.pay_time AS payTime,",
            "o.merchant_accept_time AS merchantAcceptTime,",
            "o.merchant_ready_time AS readyTime",
            "FROM order_info o",
            "LEFT JOIN user_address a ON a.id = o.delivery_address_id",
            "WHERE o.store_id = #{storeId} AND o.order_type = #{orderType}",
            "AND o.deleted = 0",
            "<if test='status != null'>AND o.status = #{status}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>",
            "AND (o.order_no LIKE CONCAT('%', #{keyword}, '%')",
            "OR EXISTS (SELECT 1 FROM order_item oi2",
            "WHERE oi2.order_id = o.id AND oi2.is_deleted = 0",
            "AND oi2.product_name LIKE CONCAT('%', #{keyword}, '%')))",
            "</if>",
            "<if test='startTime != null'>AND o.create_time &gt;= #{startTime}</if>",
            "<if test='endTime != null'>AND o.create_time &lt; #{endTime}</if>",
            "ORDER BY o.create_time DESC, o.id DESC",
            "</script>"
    })
    Page<MerchantOrderPageVO> selectOrderPage(
            Page<MerchantOrderPageVO> page,
            @Param("storeId") Long storeId,
            @Param("orderType") Integer orderType,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select({
            "SELECT o.id AS orderId, o.order_no AS orderNo, o.status,",
            "o.pay_status AS payStatus, o.product_amount AS productAmount,",
            "o.delivery_fee AS deliveryFee, o.total_amount AS totalAmount,",
            "(SELECT GROUP_CONCAT(CONCAT(oi.product_name, ' x', oi.quantity)",
            "ORDER BY oi.id SEPARATOR '、') FROM order_item oi",
            "WHERE oi.order_id = o.id AND oi.is_deleted = 0) AS goodsSummary,",
            "a.receiver_name AS receiverName, a.receiver_phone AS receiverPhone,",
            "CONCAT_WS('', a.province, a.city, a.district, a.detail_address) AS deliveryAddress,",
            "o.create_time AS createTime, o.pay_time AS payTime,",
            "o.merchant_accept_time AS merchantAcceptTime,",
            "o.merchant_ready_time AS readyTime, o.remark,",
            "o.merchant_reject_time AS merchantRejectTime,",
            "o.merchant_reject_reason AS merchantRejectReason",
            "FROM order_info o",
            "LEFT JOIN user_address a ON a.id = o.delivery_address_id",
            "WHERE o.id = #{orderId} AND o.store_id = #{storeId}",
            "AND o.order_type = #{orderType} AND o.deleted = 0",
            "LIMIT 1"
    })
    MerchantOrderDetailVO selectOrderDetail(
            @Param("orderId") Long orderId,
            @Param("storeId") Long storeId,
            @Param("orderType") Integer orderType);
}
