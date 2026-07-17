package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.merchant.CreateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryStatusDTO;
import com.wuxin.service.MerchantCategoryService;
import com.wuxin.vo.CategoryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/category")
public class MerchantCategoryController {

    private final MerchantCategoryService merchantCategoryService;

    public MerchantCategoryController(MerchantCategoryService merchantCategoryService) {
        this.merchantCategoryService = merchantCategoryService;
    }

    @PostMapping
    public Result<CategoryVO> create(@Valid @RequestBody CreateCategoryDTO createCategoryDTO) {
        return Result.success("新增商品分类成功", merchantCategoryService.createCategory(createCategoryDTO));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody UpdateCategoryDTO updateCategoryDTO) {
        merchantCategoryService.updateCategory(id, updateCategoryDTO);
        return Result.success("修改商品分类成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody UpdateCategoryStatusDTO updateCategoryStatusDTO) {
        merchantCategoryService.updateCategoryStatus(id, updateCategoryStatusDTO);
        return Result.success("商品分类状态更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        merchantCategoryService.deleteCategory(id);
        return Result.success("删除商品分类成功", null);
    }

    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(merchantCategoryService.getCategoryList());
    }
}
