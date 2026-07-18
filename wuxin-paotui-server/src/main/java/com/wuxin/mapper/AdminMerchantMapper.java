package com.wuxin.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.vo.AdminMerchantDetailVO;
import com.wuxin.vo.AdminMerchantPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMerchantMapper {

    @Select({
            "<script>",
            "SELECT m.id AS merchantId, m.user_id AS userId,",
            "m.merchant_name AS merchantName, m.contact_name AS contactName,",
            "m.contact_phone AS contactPhone, m.audit_status AS auditStatus,",
            "m.merchant_status AS merchantStatus, m.create_time AS applyTime,",
            "m.audit_time AS auditTime, s.id AS storeId, s.store_name AS storeName,",
            "s.store_status AS storeStatus, s.business_status AS businessStatus",
            "FROM merchant_info m",
            "LEFT JOIN merchant_store s ON s.merchant_id = m.id AND s.is_deleted = 0",
            "WHERE m.is_deleted = 0",
            "<if test='auditStatus != null'>AND m.audit_status = #{auditStatus}</if>",
            "<if test='merchantStatus != null'>AND m.merchant_status = #{merchantStatus}</if>",
            "<if test='keyword != null and keyword != &quot;&quot;'>",
            "AND (m.merchant_name LIKE CONCAT('%', #{keyword}, '%')",
            "OR m.contact_name LIKE CONCAT('%', #{keyword}, '%')",
            "OR m.contact_phone LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "ORDER BY m.create_time DESC, m.id DESC",
            "</script>"
    })
    Page<AdminMerchantPageVO> selectMerchantPage(
            Page<AdminMerchantPageVO> page,
            @Param("auditStatus") Integer auditStatus,
            @Param("merchantStatus") Integer merchantStatus,
            @Param("keyword") String keyword);

    @Select({
            "SELECT m.id AS merchantId, m.user_id AS userId,",
            "m.merchant_name AS merchantName, m.contact_name AS contactName,",
            "m.contact_phone AS contactPhone, m.business_license AS businessLicense,",
            "m.id_card_front AS idCardFront, m.id_card_back AS idCardBack,",
            "m.audit_status AS auditStatus, m.audit_remark AS auditRemark,",
            "m.audit_admin_id AS auditAdminId, m.audit_time AS auditTime,",
            "m.reject_reason AS rejectReason, m.merchant_status AS merchantStatus,",
            "m.create_time AS applyTime, m.update_time AS updateTime,",
            "u.username, u.nickname, u.avatar, u.phone AS userPhone, u.status AS userStatus,",
            "au.username AS auditAdminUsername,",
            "s.id AS storeId, s.store_name AS storeName, s.store_logo AS storeLogo,",
            "s.store_description AS storeDescription, s.store_phone AS storePhone,",
            "s.province, s.city, s.district, s.detail_address AS detailAddress,",
            "s.latitude, s.longitude, s.business_status AS businessStatus,",
            "s.open_time AS openTime, s.close_time AS closeTime, s.store_status AS storeStatus",
            "FROM merchant_info m",
            "LEFT JOIN sys_user u ON u.id = m.user_id",
            "LEFT JOIN sys_user au ON au.id = m.audit_admin_id",
            "LEFT JOIN merchant_store s ON s.merchant_id = m.id AND s.is_deleted = 0",
            "WHERE m.id = #{merchantId} AND m.is_deleted = 0",
            "LIMIT 1"
    })
    AdminMerchantDetailVO selectMerchantDetail(
            @Param("merchantId") Long merchantId);
}
