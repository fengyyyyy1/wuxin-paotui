package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.entity.MerchantProductEntity;
import com.wuxin.vo.ProductVO;
import com.wuxin.vo.PublicProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantProductMapper extends BaseMapper<MerchantProductEntity> {

    @Select({
            "<script>",
            "SELECT p.id AS productId, p.store_id AS storeId, p.category_id AS categoryId,",
            "c.category_name AS categoryName, p.product_name AS productName,",
            "p.product_image AS productImage, p.product_description AS productDescription,",
            "p.price, p.original_price AS originalPrice, p.stock, p.sales,",
            "p.product_status AS productStatus, p.sort,",
            "p.create_time AS createTime, p.update_time AS updateTime",
            "FROM merchant_product p",
            "INNER JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "WHERE p.store_id = #{storeId} AND p.is_deleted = 0 AND c.is_deleted = 0",
            "<if test='categoryId != null'>",
            "AND p.category_id = #{categoryId}",
            "</if>",
            "<if test='productStatus != null'>",
            "AND p.product_status = #{productStatus}",
            "</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>",
            "AND p.product_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "ORDER BY p.sort ASC, p.create_time DESC",
            "</script>"
    })
    Page<ProductVO> selectManagementPage(Page<ProductVO> page,
                                         @Param("storeId") Long storeId,
                                         @Param("categoryId") Long categoryId,
                                         @Param("productStatus") Integer productStatus,
                                         @Param("keyword") String keyword);

    @Select({
            "<script>",
            "SELECT p.id AS productId, p.category_id AS categoryId,",
            "c.category_name AS categoryName, p.product_name AS productName,",
            "p.product_image AS productImage, p.product_description AS productDescription,",
            "p.price, p.original_price AS originalPrice, p.stock, p.sales, p.sort",
            "FROM merchant_product p",
            "INNER JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "INNER JOIN merchant_store s ON s.id = p.store_id",
            "INNER JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE p.store_id = #{storeId} AND p.product_status = 1",
            "AND p.is_deleted = 0 AND p.stock &gt; 0",
            "AND c.status = 1 AND c.is_deleted = 0",
            "AND s.business_status = 1 AND s.store_status = 1 AND s.is_deleted = 0",
            "AND m.audit_status = 1 AND m.merchant_status = 1 AND m.is_deleted = 0",
            "<if test='categoryId != null'>",
            "AND p.category_id = #{categoryId}",
            "</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>",
            "AND p.product_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "ORDER BY p.sort ASC, p.create_time DESC",
            "</script>"
    })
    Page<PublicProductVO> selectPublicPage(Page<PublicProductVO> page,
                                           @Param("storeId") Long storeId,
                                           @Param("categoryId") Long categoryId,
                                           @Param("keyword") String keyword);

    @Select({
            "SELECT p.id AS productId, p.category_id AS categoryId,",
            "c.category_name AS categoryName, p.product_name AS productName,",
            "p.product_image AS productImage, p.product_description AS productDescription,",
            "p.price, p.original_price AS originalPrice, p.stock, p.sales, p.sort",
            "FROM merchant_product p",
            "INNER JOIN merchant_category c ON c.id = p.category_id AND c.store_id = p.store_id",
            "INNER JOIN merchant_store s ON s.id = p.store_id",
            "INNER JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE p.id = #{id} AND p.product_status = 1 AND p.is_deleted = 0 AND p.stock > 0",
            "AND c.status = 1 AND c.is_deleted = 0",
            "AND s.business_status = 1 AND s.store_status = 1 AND s.is_deleted = 0",
            "AND m.audit_status = 1 AND m.merchant_status = 1 AND m.is_deleted = 0",
            "LIMIT 1"
    })
    PublicProductVO selectPublicDetail(@Param("id") Long id);
}
