package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuxin.entity.OrderItemEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItemEntity> {

    @Insert({
            "<script>",
            "INSERT INTO order_item (order_id, product_id, product_name, product_image,",
            "product_price, quantity, subtotal, create_time, update_time, is_deleted) VALUES",
            "<foreach collection='items' item='item' separator=','>",
            "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productImage},",
            "#{item.productPrice}, #{item.quantity}, #{item.subtotal},",
            "#{item.createTime}, #{item.updateTime}, #{item.isDeleted})",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("items") List<OrderItemEntity> items);

    @Select({
            "SELECT id, order_id AS orderId, product_id AS productId,",
            "product_name AS productName, product_image AS productImage,",
            "product_price AS productPrice, quantity, subtotal,",
            "create_time AS createTime, update_time AS updateTime, is_deleted AS isDeleted",
            "FROM order_item",
            "WHERE order_id = #{orderId} AND is_deleted = 0",
            "ORDER BY id ASC"
    })
    List<OrderItemEntity> selectByOrderId(@Param("orderId") Long orderId);
}
