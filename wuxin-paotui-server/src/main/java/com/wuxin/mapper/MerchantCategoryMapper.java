package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wuxin.entity.MerchantCategoryEntity;
import com.wuxin.vo.CategoryVO;
import com.wuxin.vo.PublicCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MerchantCategoryMapper extends BaseMapper<MerchantCategoryEntity> {

    @Select({
            "SELECT id AS categoryId, category_name AS categoryName, sort, status, create_time AS createTime",
            "FROM merchant_category",
            "WHERE store_id = #{storeId} AND is_deleted = 0",
            "ORDER BY sort ASC, create_time ASC"
    })
    List<CategoryVO> selectManagementList(@Param("storeId") Long storeId);

    @Select({
            "SELECT c.id AS categoryId, c.category_name AS categoryName, c.sort",
            "FROM merchant_category c",
            "INNER JOIN merchant_store s ON s.id = c.store_id",
            "INNER JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE c.store_id = #{storeId} AND c.status = 1 AND c.is_deleted = 0",
            "AND s.business_status = 1 AND s.store_status = 1 AND s.is_deleted = 0",
            "AND m.audit_status = 1 AND m.merchant_status = 1 AND m.is_deleted = 0",
            "ORDER BY c.sort ASC, c.create_time ASC"
    })
    List<PublicCategoryVO> selectPublicList(@Param("storeId") Long storeId);
}
