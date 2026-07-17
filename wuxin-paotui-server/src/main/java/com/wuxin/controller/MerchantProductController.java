package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.merchant.CreateProductDTO;
import com.wuxin.dto.merchant.UpdateProductDTO;
import com.wuxin.dto.merchant.UpdateProductStatusDTO;
import com.wuxin.service.MerchantProductService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/product")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    public MerchantProductController(MerchantProductService merchantProductService) {
        this.merchantProductService = merchantProductService;
    }

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody CreateProductDTO createProductDTO) {
        return Result.success("新增商品成功", merchantProductService.createProduct(createProductDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody UpdateProductDTO updateProductDTO) {
        merchantProductService.updateProduct(id, updateProductDTO);
        return Result.success("修改商品成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UpdateProductStatusDTO updateProductStatusDTO) {
        String message = merchantProductService.updateProductStatus(id, updateProductStatusDTO);
        return Result.success(message, null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        merchantProductService.deleteProduct(id);
        return Result.success("删除商品成功", null);
    }

    @GetMapping("/list")
    public Result<PageResultVO<ProductVO>> list(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "productStatus", required = false) Integer productStatus,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(merchantProductService.getProductList(
                pageNum, pageSize, categoryId, productStatus, keyword));
    }
}
