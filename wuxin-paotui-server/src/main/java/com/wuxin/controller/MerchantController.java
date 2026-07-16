package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.merchant.MerchantApplyDTO;
import com.wuxin.dto.merchant.UpdateBusinessStatusDTO;
import com.wuxin.dto.merchant.UpdateMerchantStoreDTO;
import com.wuxin.service.MerchantService;
import com.wuxin.vo.MerchantApplyVO;
import com.wuxin.vo.MerchantDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/apply")
    public Result<MerchantApplyVO> apply(@Valid @RequestBody MerchantApplyDTO merchantApplyDTO) {
        return Result.success("商家入驻申请提交成功", merchantService.apply(merchantApplyDTO));
    }

    @GetMapping("/me")
    public Result<MerchantDetailVO> me() {
        return Result.success(merchantService.getCurrentMerchant());
    }

    @PutMapping("/store")
    public Result<Void> updateStore(@Valid @RequestBody UpdateMerchantStoreDTO updateMerchantStoreDTO) {
        merchantService.updateStore(updateMerchantStoreDTO);
        return Result.success("更新店铺资料成功", null);
    }

    @PutMapping("/store/business-status")
    public Result<Void> updateBusinessStatus(
            @Valid @RequestBody UpdateBusinessStatusDTO updateBusinessStatusDTO) {
        merchantService.updateBusinessStatus(updateBusinessStatusDTO);
        return Result.success("营业状态更新成功", null);
    }
}
