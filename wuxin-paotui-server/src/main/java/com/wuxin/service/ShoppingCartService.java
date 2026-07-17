package com.wuxin.service;

import com.wuxin.dto.cart.AddCartDTO;
import com.wuxin.dto.cart.UpdateCartDTO;
import com.wuxin.dto.cart.UpdateCartSelectedDTO;
import com.wuxin.vo.CartItemVO;
import com.wuxin.vo.CartListVO;

public interface ShoppingCartService {

    CartItemVO addCart(AddCartDTO addCartDTO);

    CartListVO getCartList();

    CartItemVO updateCart(UpdateCartDTO updateCartDTO);

    CartItemVO updateSelected(UpdateCartSelectedDTO updateCartSelectedDTO);

    void deleteCart(Long id);

    void clearCart();
}
