package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.StoreService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.StoreDetailVO;
import com.wuxin.vo.StoreListVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/list")
    public Result<PageResultVO<StoreListVO>> list(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "businessStatus", required = false) Integer businessStatus) {
        return Result.success(storeService.getStoreList(pageNum, pageSize, keyword, district, businessStatus));
    }

    @GetMapping("/{id}")
    public Result<StoreDetailVO> detail(@PathVariable Long id) {
        return Result.success(storeService.getStoreDetail(id));
    }
}
