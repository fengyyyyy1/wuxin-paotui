package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.cart.AddCartDTO;
import com.wuxin.dto.cart.UpdateCartAllSelectedDTO;
import com.wuxin.dto.cart.UpdateCartDTO;
import com.wuxin.dto.cart.UpdateCartSelectedDTO;
import com.wuxin.entity.ShoppingCartEntity;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.enums.CategoryStatusEnum;
import com.wuxin.enums.MerchantAuditStatusEnum;
import com.wuxin.enums.MerchantStatusEnum;
import com.wuxin.enums.ProductStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.ShoppingCartMapper;
import com.wuxin.service.ShoppingCartService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.CartItemQueryVO;
import com.wuxin.vo.CartItemVO;
import com.wuxin.vo.CartListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final int SELECTED = 1;

    private static final int NOT_DELETED = 0;

    private static final int DELETED = 1;

    private final ShoppingCartMapper shoppingCartMapper;

    public ShoppingCartServiceImpl(ShoppingCartMapper shoppingCartMapper) {
        this.shoppingCartMapper = shoppingCartMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO addCart(AddCartDTO addCartDTO) {
        Long userId = getCurrentUserId();
        lockUser(userId);

        CartItemQueryVO productState = shoppingCartMapper.selectProductState(addCartDTO.getProductId());
        validateProductForWrite(productState, addCartDTO.getQuantity());

        Long activeStoreId = shoppingCartMapper.selectActiveStoreId(userId);
        if (activeStoreId != null && !activeStoreId.equals(productState.getStoreId())) {
            throw new BusinessException(ResultCode.CART_STORE_CONFLICT);
        }

        ShoppingCartEntity activeCart = shoppingCartMapper.selectActiveByProduct(
                userId, addCartDTO.getProductId());
        LocalDateTime now = LocalDateTime.now();
        Long cartId;
        if (activeCart != null) {
            long accumulatedQuantity = (long) activeCart.getQuantity() + addCartDTO.getQuantity();
            if (accumulatedQuantity > Integer.MAX_VALUE) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
            }
            int newQuantity = (int) accumulatedQuantity;
            validateStock(productState.getStock(), newQuantity);
            updateQuantity(activeCart.getId(), userId, newQuantity, now);
            cartId = activeCart.getId();
        } else {
            ShoppingCartEntity deletedCart = shoppingCartMapper.selectDeletedByProduct(
                    userId, addCartDTO.getProductId());
            if (deletedCart != null) {
                int affectedRows = shoppingCartMapper.reviveDeletedCart(
                        deletedCart.getId(), userId, productState.getStoreId(),
                        productState.getProductId(), addCartDTO.getQuantity(), now);
                if (affectedRows != 1) {
                    throw new BusinessException(ResultCode.CART_NOT_EXIST);
                }
                cartId = deletedCart.getId();
            } else {
                ShoppingCartEntity cart = buildCart(
                        userId, productState.getStoreId(), productState.getProductId(),
                        addCartDTO.getQuantity(), now);
                if (shoppingCartMapper.insert(cart) != 1 || cart.getId() == null) {
                    throw new IllegalStateException("shopping cart save failed");
                }
                cartId = cart.getId();
            }
        }
        return getValidCartItem(cartId, userId);
    }

    @Override
    public CartListVO getCartList() {
        Long userId = getCurrentUserId();
        List<CartItemQueryVO> queryItems = shoppingCartMapper.selectCartItems(userId);
        List<CartItemVO> items = new ArrayList<>(queryItems.size());
        BigDecimal selectedTotalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        long selectedProductCount = 0L;

        for (CartItemQueryVO queryItem : queryItems) {
            CartItemVO item = toCartItemVO(queryItem);
            items.add(item);
            if (SELECTED == defaultZero(item.getSelected()) && item.getInvalidReason() == null) {
                selectedTotalAmount = selectedTotalAmount.add(item.getSubtotal());
                selectedProductCount = Math.addExact(selectedProductCount, item.getQuantity().longValue());
            }
        }

        CartListVO cartListVO = new CartListVO();
        if (!items.isEmpty()) {
            cartListVO.setStoreId(items.get(0).getStoreId());
            cartListVO.setStoreName(items.get(0).getStoreName());
        }
        cartListVO.setItems(items);
        cartListVO.setSelectedTotalAmount(money(selectedTotalAmount));
        cartListVO.setSelectedProductCount(selectedProductCount);
        return cartListVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO updateCart(UpdateCartDTO updateCartDTO) {
        Long userId = getCurrentUserId();
        lockUser(userId);
        ShoppingCartEntity cart = getActiveCart(updateCartDTO.getCartId(), userId);
        CartItemQueryVO productState = shoppingCartMapper.selectProductState(cart.getProductId());
        validateProductForWrite(productState, updateCartDTO.getQuantity());
        if (!cart.getStoreId().equals(productState.getStoreId())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }

        updateQuantity(cart.getId(), userId, updateCartDTO.getQuantity(), LocalDateTime.now());
        return getValidCartItem(cart.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartItemVO updateSelected(UpdateCartSelectedDTO updateCartSelectedDTO) {
        Long userId = getCurrentUserId();
        lockUser(userId);
        ShoppingCartEntity cart = getActiveCart(updateCartSelectedDTO.getCartId(), userId);
        if (SELECTED == updateCartSelectedDTO.getSelected()) {
            CartItemQueryVO productState = shoppingCartMapper.selectProductState(cart.getProductId());
            validateProductForWrite(productState, cart.getQuantity());
            if (!cart.getStoreId().equals(productState.getStoreId())) {
                throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
            }
        }

        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getId, cart.getId())
                .eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getSelected, updateCartSelectedDTO.getSelected())
                .set(ShoppingCartEntity::getUpdateTime, LocalDateTime.now());
        if (shoppingCartMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
        return getCartItem(cart.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CartListVO updateAllSelected(UpdateCartAllSelectedDTO updateCartAllSelectedDTO) {
        Long userId = getCurrentUserId();
        lockUser(userId);
        List<CartItemQueryVO> queryItems = shoppingCartMapper.selectCartItems(userId);
        LocalDateTime now = LocalDateTime.now();

        for (CartItemQueryVO queryItem : queryItems) {
            if (resolveInvalidReason(queryItem) != null) {
                continue;
            }
            updateSelectedValue(queryItem.getCartId(), userId, updateCartAllSelectedDTO.getSelected(), now);
        }

        return getCartList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(Long id) {
        validateId(id);
        Long userId = getCurrentUserId();
        lockUser(userId);
        getActiveCart(id, userId);

        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getId, id)
                .eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getIsDeleted, DELETED)
                .set(ShoppingCartEntity::getUpdateTime, LocalDateTime.now());
        if (shoppingCartMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearInvalidCart() {
        Long userId = getCurrentUserId();
        lockUser(userId);
        List<CartItemQueryVO> queryItems = shoppingCartMapper.selectCartItems(userId);
        LocalDateTime now = LocalDateTime.now();

        for (CartItemQueryVO queryItem : queryItems) {
            if (resolveInvalidReason(queryItem) != null) {
                logicalDeleteCartItem(queryItem.getCartId(), userId, now);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart() {
        Long userId = getCurrentUserId();
        lockUser(userId);

        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getIsDeleted, DELETED)
                .set(ShoppingCartEntity::getUpdateTime, LocalDateTime.now());
        shoppingCartMapper.update(null, updateWrapper);
    }

    private ShoppingCartEntity buildCart(Long userId, Long storeId, Long productId,
                                         Integer quantity, LocalDateTime now) {
        ShoppingCartEntity cart = new ShoppingCartEntity();
        cart.setUserId(userId);
        cart.setStoreId(storeId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setSelected(SELECTED);
        cart.setCreateTime(now);
        cart.setUpdateTime(now);
        cart.setIsDeleted(NOT_DELETED);
        return cart;
    }

    private void updateQuantity(Long cartId, Long userId, Integer quantity, LocalDateTime now) {
        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getId, cartId)
                .eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getQuantity, quantity)
                .set(ShoppingCartEntity::getUpdateTime, now);
        if (shoppingCartMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
    }

    private void updateSelectedValue(Long cartId, Long userId, Integer selected, LocalDateTime now) {
        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getId, cartId)
                .eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getSelected, selected)
                .set(ShoppingCartEntity::getUpdateTime, now);
        if (shoppingCartMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
    }

    private void logicalDeleteCartItem(Long cartId, Long userId, LocalDateTime now) {
        LambdaUpdateWrapper<ShoppingCartEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ShoppingCartEntity::getId, cartId)
                .eq(ShoppingCartEntity::getUserId, userId)
                .eq(ShoppingCartEntity::getIsDeleted, NOT_DELETED)
                .set(ShoppingCartEntity::getIsDeleted, DELETED)
                .set(ShoppingCartEntity::getUpdateTime, now);
        if (shoppingCartMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
    }

    private ShoppingCartEntity getActiveCart(Long cartId, Long userId) {
        ShoppingCartEntity cart = shoppingCartMapper.selectActiveById(cartId, userId);
        if (cart == null) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
        return cart;
    }

    private CartItemVO getValidCartItem(Long cartId, Long userId) {
        CartItemVO cartItem = getCartItem(cartId, userId);
        if (cartItem.getInvalidReason() != null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        return cartItem;
    }

    private CartItemVO getCartItem(Long cartId, Long userId) {
        CartItemQueryVO queryItem = shoppingCartMapper.selectCartItem(cartId, userId);
        if (queryItem == null) {
            throw new BusinessException(ResultCode.CART_NOT_EXIST);
        }
        return toCartItemVO(queryItem);
    }

    private CartItemVO toCartItemVO(CartItemQueryVO queryItem) {
        CartItemVO item = new CartItemVO();
        item.setCartId(queryItem.getCartId());
        item.setStoreId(queryItem.getStoreId());
        item.setStoreName(queryItem.getStoreName());
        item.setProductId(queryItem.getProductId());
        item.setProductName(queryItem.getProductName());
        item.setProductImage(queryItem.getProductImage());
        item.setPrice(queryItem.getPrice() == null ? null : money(queryItem.getPrice()));
        item.setStock(queryItem.getStock());
        item.setQuantity(queryItem.getQuantity());
        item.setSelected(queryItem.getSelected());
        item.setProductStatus(queryItem.getProductStatus());
        item.setInvalidReason(resolveInvalidReason(queryItem));
        item.setSubtotal(calculateSubtotal(queryItem.getPrice(), queryItem.getQuantity()));
        return item;
    }

    private void validateProductForWrite(CartItemQueryVO product, Integer quantity) {
        if (product == null || !isOne(product.getProductExists()) || isOne(product.getProductDeleted())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (!ProductStatusEnum.ON_SHELF.getCode().equals(product.getProductStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }
        if (!isOne(product.getCategoryExists())
                || isOne(product.getCategoryDeleted())
                || !CategoryStatusEnum.ENABLED.getCode().equals(product.getCategoryStatus())) {
            throw new BusinessException(ResultCode.CATEGORY_DISABLED);
        }
        if (!isOne(product.getStoreExists())
                || isOne(product.getStoreDeleted())
                || !MerchantStatusEnum.ENABLED.getCode().equals(product.getStoreStatus())) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        if (!BusinessStatusEnum.OPEN.getCode().equals(product.getBusinessStatus())) {
            throw new BusinessException(ResultCode.STORE_CLOSED);
        }
        if (!isOne(product.getMerchantExists())
                || isOne(product.getMerchantDeleted())
                || !MerchantAuditStatusEnum.APPROVED.getCode().equals(product.getMerchantAuditStatus())
                || !MerchantStatusEnum.ENABLED.getCode().equals(product.getMerchantStatus())) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        validateStock(product.getStock(), quantity);
    }

    private String resolveInvalidReason(CartItemQueryVO item) {
        if (!isOne(item.getProductExists()) || isOne(item.getProductDeleted())) {
            return "商品已删除";
        }
        if (!ProductStatusEnum.ON_SHELF.getCode().equals(item.getProductStatus())) {
            return "商品已下架";
        }
        if (!isOne(item.getCategoryExists()) || isOne(item.getCategoryDeleted())) {
            return "商品分类已删除";
        }
        if (!CategoryStatusEnum.ENABLED.getCode().equals(item.getCategoryStatus())) {
            return "商品分类已禁用";
        }
        if (!isOne(item.getStoreExists()) || isOne(item.getStoreDeleted())) {
            return "店铺不存在";
        }
        if (!MerchantStatusEnum.ENABLED.getCode().equals(item.getStoreStatus())) {
            return "店铺已禁用";
        }
        if (!BusinessStatusEnum.OPEN.getCode().equals(item.getBusinessStatus())) {
            return "店铺已停业";
        }
        if (!isOne(item.getMerchantExists())
                || isOne(item.getMerchantDeleted())
                || !MerchantAuditStatusEnum.APPROVED.getCode().equals(item.getMerchantAuditStatus())
                || !MerchantStatusEnum.ENABLED.getCode().equals(item.getMerchantStatus())) {
            return "商家不可用";
        }
        if (item.getStock() == null || item.getStock() <= 0 || item.getQuantity() > item.getStock()) {
            return "商品库存不足";
        }
        return null;
    }

    private void validateStock(Integer stock, Integer quantity) {
        if (stock == null || stock <= 0 || quantity == null || quantity > stock) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
        }
    }

    private BigDecimal calculateSubtotal(BigDecimal price, Integer quantity) {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return money(price.multiply(BigDecimal.valueOf(quantity)));
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isOne(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void lockUser(Long userId) {
        if (shoppingCartMapper.lockUser(userId) == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
    }
}
