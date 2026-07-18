package com.wuxin.service;

import com.wuxin.dto.admin.AdminMerchantPageQueryDTO;
import com.wuxin.dto.admin.AdminMerchantReasonDTO;
import com.wuxin.dto.admin.ApproveMerchantDTO;
import com.wuxin.vo.AdminMerchantDetailVO;
import com.wuxin.vo.AdminMerchantOperationVO;
import com.wuxin.vo.AdminMerchantPageVO;
import com.wuxin.vo.PageResultVO;

public interface AdminMerchantService {

    PageResultVO<AdminMerchantPageVO> page(
            AdminMerchantPageQueryDTO query);

    AdminMerchantDetailVO detail(Long merchantId);

    AdminMerchantOperationVO approve(
            Long merchantId,
            ApproveMerchantDTO request);

    AdminMerchantOperationVO reject(
            Long merchantId,
            AdminMerchantReasonDTO request);

    AdminMerchantOperationVO enable(Long merchantId);

    AdminMerchantOperationVO disable(
            Long merchantId,
            AdminMerchantReasonDTO request);
}
