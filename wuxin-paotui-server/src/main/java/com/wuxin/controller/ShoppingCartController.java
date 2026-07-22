package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.cart.AddCartDTO;
import com.wuxin.dto.cart.UpdateCartAllSelectedDTO;
import com.wuxin.dto.cart.UpdateCartDTO;
import com.wuxin.dto.cart.UpdateCartSelectedDTO;
import com.wuxin.service.ShoppingCartService;
import com.wuxin.vo.CartItemVO;
import com.wuxin.vo.CartListVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/add")
    public Result<CartItemVO> add(@Valid @RequestBody AddCartDTO addCartDTO) {
        return Result.success("加入购物车成功", shoppingCartService.addCart(addCartDTO));
    }

    @GetMapping("/list")
    public Result<CartListVO> list() {
        return Result.success(shoppingCartService.getCartList());
    }

    @PutMapping("/update")
    public Result<CartItemVO> update(@Valid @RequestBody UpdateCartDTO updateCartDTO) {
        return Result.success("购物车数量更新成功", shoppingCartService.updateCart(updateCartDTO));
    }

    @PutMapping("/selected")
    public Result<CartItemVO> updateSelected(
            @Valid @RequestBody UpdateCartSelectedDTO updateCartSelectedDTO) {
        return Result.success(
                "购物车选中状态更新成功",
                shoppingCartService.updateSelected(updateCartSelectedDTO));
    }

    @PutMapping("/selected/all")
    public Result<CartListVO> updateAllSelected(
            @Valid @RequestBody UpdateCartAllSelectedDTO updateCartAllSelectedDTO) {
        return Result.success(
                "购物车全选状态更新成功",
                shoppingCartService.updateAllSelected(updateCartAllSelectedDTO));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shoppingCartService.deleteCart(id);
        return Result.success("删除购物车商品成功", null);
    }

    @DeleteMapping("/invalid")
    public Result<Void> clearInvalid() {
        shoppingCartService.clearInvalidCart();
        return Result.success("失效商品清理成功", null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        shoppingCartService.clearCart();
        return Result.success("清空购物车成功", null);
    }
}
