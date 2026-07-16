package com.wuxin.service;

import com.wuxin.dto.merchant.MerchantApplyDTO;
import com.wuxin.dto.merchant.UpdateBusinessStatusDTO;
import com.wuxin.dto.merchant.UpdateMerchantStoreDTO;
import com.wuxin.vo.MerchantApplyVO;
import com.wuxin.vo.MerchantDetailVO;

public interface MerchantService {

    MerchantApplyVO apply(MerchantApplyDTO merchantApplyDTO);

    MerchantDetailVO getCurrentMerchant();

    void updateStore(UpdateMerchantStoreDTO updateMerchantStoreDTO);

    void updateBusinessStatus(UpdateBusinessStatusDTO updateBusinessStatusDTO);
}
