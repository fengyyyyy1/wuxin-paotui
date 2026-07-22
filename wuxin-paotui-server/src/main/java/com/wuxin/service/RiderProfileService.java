package com.wuxin.service;

import com.wuxin.dto.rider.RiderApplyDTO;
import com.wuxin.vo.RiderApplyVO;
import com.wuxin.vo.RiderProfileVO;

public interface RiderProfileService {

    RiderApplyVO apply(RiderApplyDTO request);

    RiderProfileVO getCurrentProfile();
}
