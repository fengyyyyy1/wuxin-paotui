package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.StoreProductService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.PublicCategoryVO;
import com.wuxin.vo.PublicProductVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store")
public class StoreProductController {

    private final StoreProductService storeProductService;

    public StoreProductController(StoreProductService storeProductService) {
        this.storeProductService = storeProductService;
    }

    @GetMapping("/{storeId}/categories")
    public Result<List<PublicCategoryVO>> categories(@PathVariable Long storeId) {
        return Result.success(storeProductService.getCategoryList(storeId));
    }

    @GetMapping("/{storeId}/products")
    public Result<PageResultVO<PublicProductVO>> products(
            @PathVariable Long storeId,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(storeProductService.getProductList(
                storeId, pageNum, pageSize, categoryId, keyword));
    }

    @GetMapping("/product/{id}")
    public Result<PublicProductVO> productDetail(@PathVariable Long id) {
        return Result.success(storeProductService.getProductDetail(id));
    }
}
