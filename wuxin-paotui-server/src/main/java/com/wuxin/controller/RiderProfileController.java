package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.rider.RiderApplyDTO;
import com.wuxin.service.RiderProfileService;
import com.wuxin.vo.RiderApplyVO;
import com.wuxin.vo.RiderProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider")
public class RiderProfileController {

    private final RiderProfileService riderProfileService;

    public RiderProfileController(RiderProfileService riderProfileService) {
        this.riderProfileService = riderProfileService;
    }

    @PostMapping("/apply")
    public Result<RiderApplyVO> apply(@Valid @RequestBody RiderApplyDTO request) {
        return Result.success("骑手申请提交成功", riderProfileService.apply(request));
    }

    @GetMapping("/profile")
    public Result<RiderProfileVO> profile() {
        return Result.success(riderProfileService.getCurrentProfile());
    }
}
