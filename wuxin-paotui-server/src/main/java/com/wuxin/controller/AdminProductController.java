package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminBusinessService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@AdminPermission("product:view")
public class AdminProductController {
    private final AdminBusinessService service;

    public AdminProductController(AdminBusinessService service) {
        this.service = service;
    }

    @GetMapping
    public Result<PageResultVO<AdminConsoleVO.ProductRow>> page(@Valid AdminConsoleDTO.ProductQuery query) {
        return Result.success(service.pageProducts(query));
    }

    @GetMapping("/categories")
    public Result<List<AdminConsoleVO.CategoryRow>> categories() {
        return Result.success(service.categories());
    }

    @PutMapping("/{id}/status")
    @AdminPermission("product:manage")
    public Result<AdminConsoleVO.ProductRow> status(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.StatusUpdate request) {
        return Result.success("商品状态已更新", service.updateProductStatus(id, request));
    }

    @PutMapping("/{id}/flags")
    @AdminPermission("product:manage")
    public Result<AdminConsoleVO.ProductRow> flags(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.ProductFlags request) {
        return Result.success("商品运营标记已更新", service.updateProductFlags(id, request));
    }
}
