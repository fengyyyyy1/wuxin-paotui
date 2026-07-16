package com.wuxin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.entity.MerchantStoreEntity;
import com.wuxin.vo.StoreDetailVO;
import com.wuxin.vo.StoreListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantStoreMapper extends BaseMapper<MerchantStoreEntity> {

    @Select({
            "<script>",
            "SELECT s.id AS storeId, s.merchant_id AS merchantId, s.store_name AS storeName,",
            "s.store_logo AS storeLogo, s.store_description AS storeDescription,",
            "s.store_phone AS storePhone, s.district, s.detail_address AS detailAddress,",
            "s.business_status AS businessStatus, s.open_time AS openTime, s.close_time AS closeTime",
            "FROM merchant_store s INNER JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE m.audit_status = 1 AND m.merchant_status = 1 AND m.is_deleted = 0",
            "AND s.store_status = 1 AND s.is_deleted = 0",
            "<if test='keyword != null and keyword != &quot;&quot;'>",
            "AND s.store_name LIKE CONCAT('%', #{keyword}, '%')",
            "</if>",
            "<if test='district != null and district != &quot;&quot;'>",
            "AND s.district = #{district}",
            "</if>",
            "<if test='businessStatus != null'>",
            "AND s.business_status = #{businessStatus}",
            "</if>",
            "ORDER BY s.business_status DESC, s.create_time DESC",
            "</script>"
    })
    Page<StoreListVO> selectStorePage(Page<StoreListVO> page,
                                      @Param("keyword") String keyword,
                                      @Param("district") String district,
                                      @Param("businessStatus") Integer businessStatus);

    @Select({
            "SELECT s.id AS storeId, s.merchant_id AS merchantId, m.merchant_name AS merchantName,",
            "s.store_name AS storeName, s.store_logo AS storeLogo,",
            "s.store_description AS storeDescription, s.store_phone AS storePhone,",
            "s.province, s.city, s.district, s.detail_address AS detailAddress,",
            "s.latitude, s.longitude, s.business_status AS businessStatus,",
            "s.open_time AS openTime, s.close_time AS closeTime",
            "FROM merchant_store s INNER JOIN merchant_info m ON m.id = s.merchant_id",
            "WHERE s.id = #{id} AND m.audit_status = 1 AND m.merchant_status = 1",
            "AND m.is_deleted = 0 AND s.store_status = 1 AND s.is_deleted = 0",
            "LIMIT 1"
    })
    StoreDetailVO selectStoreDetail(@Param("id") Long id);
}
