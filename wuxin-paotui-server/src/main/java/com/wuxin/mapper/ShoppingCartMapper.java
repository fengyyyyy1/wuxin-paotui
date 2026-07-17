package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuxin.entity.ShoppingCartEntity;
import com.wuxin.vo.CartItemQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCartEntity> {

    @Select({
            "SELECT id FROM sys_user",
            "WHERE id = #{userId} AND is_deleted = 0",
            "FOR UPDATE"
    })
    Long lockUser(@Param("userId") Long userId);

    @Select({
            "SELECT store_id FROM shopping_cart",
            "WHERE user_id = #{userId} AND is_deleted = 0",
            "LIMIT 1"
    })
    Long selectActiveStoreId(@Param("userId") Long userId);

    @Select({
            "SELECT id, user_id AS userId, store_id AS storeId, product_id AS productId,",
            "quantity, selected, create_time AS createTime, update_time AS updateTime,",
            "is_deleted AS isDeleted",
            "FROM shopping_cart",
            "WHERE user_id = #{userId} AND product_id = #{productId} AND is_deleted = 0",
            "LIMIT 1"
    })
    ShoppingCartEntity selectActiveByProduct(@Param("userId") Long userId,
                                             @Param("productId") Long productId);

    @Select({
            "SELECT id, user_id AS userId, store_id AS storeId, product_id AS productId,",
            "quantity, selected, create_time AS createTime, update_time AS updateTime,",
            "is_deleted AS isDeleted",
            "FROM shopping_cart",
            "WHERE user_id = #{userId} AND product_id = #{productId} AND is_deleted = 1",
            "LIMIT 1"
    })
    ShoppingCartEntity selectDeletedByProduct(@Param("userId") Long userId,
                                              @Param("productId") Long productId);

    @Select({
            "SELECT id, user_id AS userId, store_id AS storeId, product_id AS productId,",
            "quantity, selected, create_time AS createTime, update_time AS updateTime,",
            "is_deleted AS isDeleted",
            "FROM shopping_cart",
            "WHERE id = #{id} AND user_id = #{userId} AND is_deleted = 0",
            "LIMIT 1"
    })
    ShoppingCartEntity selectActiveById(@Param("id") Long id, @Param("userId") Long userId);

    @Update({
            "UPDATE shopping_cart",
            "SET store_id = #{storeId}, quantity = #{quantity}, selected = 1,",
            "create_time = #{now}, update_time = #{now}, is_deleted = 0",
            "WHERE id = #{id} AND user_id = #{userId} AND product_id = #{productId}",
            "AND is_deleted = 1"
    })
    int reviveDeletedCart(@Param("id") Long id,
                          @Param("userId") Long userId,
                          @Param("storeId") Long storeId,
                          @Param("productId") Long productId,
                          @Param("quantity") Integer quantity,
                          @Param("now") LocalDateTime now);

    @Select({
            "SELECT NULL AS cartId, p.store_id AS storeId, p.store_id AS productStoreId,",
            "s.store_name AS storeName,",
            "p.id AS productId, p.product_name AS productName, p.product_image AS productImage,",
            "p.price, p.stock, p.product_status AS productStatus,",
            "CASE WHEN p.id IS NULL THEN 0 ELSE 1 END AS productExists,",
            "p.is_deleted AS productDeleted,",
            "CASE WHEN c.id IS NULL THEN 0 ELSE 1 END AS categoryExists,",
            "c.status AS categoryStatus, c.is_deleted AS categoryDeleted,",
            "CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS storeExists,",
            "s.store_status AS storeStatus, s.business_status AS businessStatus,",
            "s.is_deleted AS storeDeleted,",
            "CASE WHEN m.id IS NULL THEN 0 ELSE 1 END AS merchantExists,",
            "m.audit_status AS merchantAuditStatus, m.merchant_status AS merchantStatus,",
            "m.is_deleted AS merchantDeleted",
            "FROM merchant_product p",
            "LEFT JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "LEFT JOIN merchant_store s ON s.id = p.store_id",
            "LEFT JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE p.id = #{productId}",
            "LIMIT 1"
    })
    CartItemQueryVO selectProductState(@Param("productId") Long productId);

    @Select({
            "SELECT sc.id AS cartId, sc.store_id AS storeId, s.store_name AS storeName,",
            "sc.product_id AS productId, p.product_name AS productName,",
            "p.product_image AS productImage, p.price, p.stock, sc.quantity, sc.selected,",
            "p.product_status AS productStatus,",
            "CASE WHEN p.id IS NULL THEN 0 ELSE 1 END AS productExists,",
            "p.is_deleted AS productDeleted,",
            "CASE WHEN c.id IS NULL THEN 0 ELSE 1 END AS categoryExists,",
            "c.status AS categoryStatus, c.is_deleted AS categoryDeleted,",
            "CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS storeExists,",
            "s.store_status AS storeStatus, s.business_status AS businessStatus,",
            "s.is_deleted AS storeDeleted,",
            "CASE WHEN m.id IS NULL THEN 0 ELSE 1 END AS merchantExists,",
            "m.audit_status AS merchantAuditStatus, m.merchant_status AS merchantStatus,",
            "m.is_deleted AS merchantDeleted",
            "FROM shopping_cart sc",
            "LEFT JOIN merchant_product p ON p.id = sc.product_id",
            "LEFT JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "LEFT JOIN merchant_store s ON s.id = sc.store_id",
            "LEFT JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE sc.user_id = #{userId} AND sc.is_deleted = 0",
            "ORDER BY sc.create_time ASC"
    })
    List<CartItemQueryVO> selectCartItems(@Param("userId") Long userId);

    @Select({
            "SELECT sc.id AS cartId, sc.store_id AS storeId, p.store_id AS productStoreId,",
            "s.store_name AS storeName, sc.product_id AS productId,",
            "p.product_name AS productName, p.product_image AS productImage,",
            "p.price, p.stock, sc.quantity, sc.selected, p.product_status AS productStatus,",
            "CASE WHEN p.id IS NULL THEN 0 ELSE 1 END AS productExists,",
            "p.is_deleted AS productDeleted,",
            "CASE WHEN c.id IS NULL THEN 0 ELSE 1 END AS categoryExists,",
            "c.status AS categoryStatus, c.is_deleted AS categoryDeleted,",
            "CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS storeExists,",
            "s.store_status AS storeStatus, s.business_status AS businessStatus,",
            "s.is_deleted AS storeDeleted,",
            "CASE WHEN m.id IS NULL THEN 0 ELSE 1 END AS merchantExists,",
            "m.audit_status AS merchantAuditStatus, m.merchant_status AS merchantStatus,",
            "m.is_deleted AS merchantDeleted",
            "FROM shopping_cart sc",
            "LEFT JOIN merchant_product p ON p.id = sc.product_id",
            "LEFT JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "LEFT JOIN merchant_store s ON s.id = sc.store_id",
            "LEFT JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE sc.user_id = #{userId} AND sc.selected = 1 AND sc.is_deleted = 0",
            "ORDER BY sc.create_time ASC"
    })
    List<CartItemQueryVO> selectSelectedCartItems(@Param("userId") Long userId);

    @Select({
            "SELECT sc.id AS cartId, sc.store_id AS storeId, s.store_name AS storeName,",
            "sc.product_id AS productId, p.product_name AS productName,",
            "p.product_image AS productImage, p.price, p.stock, sc.quantity, sc.selected,",
            "p.product_status AS productStatus,",
            "CASE WHEN p.id IS NULL THEN 0 ELSE 1 END AS productExists,",
            "p.is_deleted AS productDeleted,",
            "CASE WHEN c.id IS NULL THEN 0 ELSE 1 END AS categoryExists,",
            "c.status AS categoryStatus, c.is_deleted AS categoryDeleted,",
            "CASE WHEN s.id IS NULL THEN 0 ELSE 1 END AS storeExists,",
            "s.store_status AS storeStatus, s.business_status AS businessStatus,",
            "s.is_deleted AS storeDeleted,",
            "CASE WHEN m.id IS NULL THEN 0 ELSE 1 END AS merchantExists,",
            "m.audit_status AS merchantAuditStatus, m.merchant_status AS merchantStatus,",
            "m.is_deleted AS merchantDeleted",
            "FROM shopping_cart sc",
            "LEFT JOIN merchant_product p ON p.id = sc.product_id",
            "LEFT JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "LEFT JOIN merchant_store s ON s.id = sc.store_id",
            "LEFT JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE sc.id = #{cartId} AND sc.user_id = #{userId} AND sc.is_deleted = 0",
            "LIMIT 1"
    })
    CartItemQueryVO selectCartItem(@Param("cartId") Long cartId, @Param("userId") Long userId);

    @Update({
            "UPDATE shopping_cart",
            "SET is_deleted = 1, update_time = #{now}",
            "WHERE user_id = #{userId} AND selected = 1 AND is_deleted = 0"
    })
    int logicalDeleteSelected(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
