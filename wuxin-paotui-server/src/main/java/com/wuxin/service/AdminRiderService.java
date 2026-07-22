package com.wuxin.service;

import com.wuxin.dto.admin.AdminRiderReasonDTO;
import com.wuxin.vo.AdminRiderOperationVO;

public interface AdminRiderService {

    AdminRiderOperationVO approve(Long riderId);

    AdminRiderOperationVO reject(Long riderId, AdminRiderReasonDTO request);

    AdminRiderOperationVO enable(Long riderId);

    AdminRiderOperationVO disable(Long riderId, AdminRiderReasonDTO request);
}
