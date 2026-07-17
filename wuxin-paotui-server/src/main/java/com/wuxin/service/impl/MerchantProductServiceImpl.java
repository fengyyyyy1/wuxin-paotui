package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.merchant.CreateProductDTO;
import com.wuxin.dto.merchant.UpdateProductDTO;
import com.wuxin.dto.merchant.UpdateProductStatusDTO;
import com.wuxin.entity.MerchantCategoryEntity;
import com.wuxin.entity.MerchantProductEntity;
import com.wuxin.enums.CategoryStatusEnum;
import com.wuxin.enums.ProductStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantCategoryMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.service.MerchantProductService;
import com.wuxin.service.MerchantService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.ProductVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantProductServiceImpl implements MerchantProductService {

    private final MerchantService merchantService;

    private final MerchantCategoryMapper merchantCategoryMapper;

    private final MerchantProductMapper merchantProductMapper;

    public MerchantProductServiceImpl(MerchantService merchantService,
                                      MerchantCategoryMapper merchantCategoryMapper,
                                      MerchantProductMapper merchantProductMapper) {
        this.merchantService = merchantService;
        this.merchantCategoryMapper = merchantCategoryMapper;
        this.merchantProductMapper = merchantProductMapper;
    }

    @Override
    public ProductVO createProduct(CreateProductDTO createProductDTO) {
        Long storeId = merchantService.getCurrentApprovedStoreId();
        MerchantCategoryEntity category = getCategory(createProductDTO.getCategoryId(), storeId);
        LocalDateTime now = LocalDateTime.now();

        MerchantProductEntity product = new MerchantProductEntity();
        product.setStoreId(storeId);
        product.setCategoryId(category.getId());
        product.setProductName(createProductDTO.getProductName().trim());
        product.setProductImage(createProductDTO.getProductImage());
        product.setProductDescription(createProductDTO.getProductDescription());
        product.setPrice(createProductDTO.getPrice());
        product.setOriginalPrice(createProductDTO.getOriginalPrice());
        product.setStock(createProductDTO.getStock());
        product.setSales(0);
        product.setProductStatus(ProductStatusEnum.OFF_SHELF.getCode());
        product.setSort(defaultSort(createProductDTO.getSort()));
        product.setCreateTime(now);
        product.setUpdateTime(now);
        product.setIsDeleted(0);

        if (merchantProductMapper.insert(product) != 1 || product.getId() == null) {
            throw new IllegalStateException("merchant product save failed");
        }
        return toProductVO(product, category.getCategoryName());
    }

    @Override
    public void updateProduct(Long id, UpdateProductDTO updateProductDTO) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        getProduct(id, storeId);
        getCategory(updateProductDTO.getCategoryId(), storeId);

        LambdaUpdateWrapper<MerchantProductEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantProductEntity::getId, id)
                .eq(MerchantProductEntity::getStoreId, storeId)
                .eq(MerchantProductEntity::getIsDeleted, 0)
                .set(MerchantProductEntity::getCategoryId, updateProductDTO.getCategoryId())
                .set(MerchantProductEntity::getProductName, updateProductDTO.getProductName().trim())
                .set(MerchantProductEntity::getProductImage, updateProductDTO.getProductImage())
                .set(MerchantProductEntity::getProductDescription, updateProductDTO.getProductDescription())
                .set(MerchantProductEntity::getPrice, updateProductDTO.getPrice())
                .set(MerchantProductEntity::getOriginalPrice, updateProductDTO.getOriginalPrice())
                .set(MerchantProductEntity::getStock, updateProductDTO.getStock())
                .set(MerchantProductEntity::getSort, defaultSort(updateProductDTO.getSort()))
                .set(MerchantProductEntity::getUpdateTime, LocalDateTime.now());
        if (merchantProductMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
    }

    @Override
    public String updateProductStatus(Long id, UpdateProductStatusDTO updateProductStatusDTO) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        MerchantProductEntity product = getProduct(id, storeId);
        ProductStatusEnum targetStatus = ProductStatusEnum.of(updateProductStatusDTO.getProductStatus());
        if (targetStatus == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (ProductStatusEnum.ON_SHELF.equals(targetStatus)) {
            MerchantCategoryEntity category = getCategory(product.getCategoryId(), storeId);
            if (!CategoryStatusEnum.ENABLED.getCode().equals(category.getStatus())) {
                throw new BusinessException(ResultCode.CATEGORY_DISABLED);
            }
            if (product.getStock() == null || product.getStock() <= 0) {
                throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
            }
        }

        LambdaUpdateWrapper<MerchantProductEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantProductEntity::getId, id)
                .eq(MerchantProductEntity::getStoreId, storeId)
                .eq(MerchantProductEntity::getIsDeleted, 0)
                .set(MerchantProductEntity::getProductStatus, targetStatus.getCode())
                .set(MerchantProductEntity::getUpdateTime, LocalDateTime.now());
        if (merchantProductMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        return ProductStatusEnum.ON_SHELF.equals(targetStatus) ? "商品上架成功" : "商品下架成功";
    }

    @Override
    public void deleteProduct(Long id) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        getProduct(id, storeId);

        LambdaUpdateWrapper<MerchantProductEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantProductEntity::getId, id)
                .eq(MerchantProductEntity::getStoreId, storeId)
                .eq(MerchantProductEntity::getIsDeleted, 0)
                .set(MerchantProductEntity::getProductStatus, ProductStatusEnum.OFF_SHELF.getCode())
                .set(MerchantProductEntity::getIsDeleted, 1)
                .set(MerchantProductEntity::getUpdateTime, LocalDateTime.now());
        if (merchantProductMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
    }

    @Override
    public PageResultVO<ProductVO> getProductList(Integer pageNum, Integer pageSize, Long categoryId,
                                                   Integer productStatus, String keyword) {
        Long storeId = merchantService.getCurrentApprovedStoreId();
        if (categoryId != null && categoryId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        if (productStatus != null && ProductStatusEnum.of(productStatus) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);
        Page<ProductVO> page = merchantProductMapper.selectManagementPage(
                new Page<>(safePageNum, safePageSize), storeId, categoryId, productStatus, normalize(keyword));
        List<ProductVO> records = page.getRecords();
        records.forEach(product -> product.setProductStatusText(
                ProductStatusEnum.getTextByCode(product.getProductStatus())));
        return buildPageResult(page, records, safePageNum, safePageSize);
    }

    private MerchantCategoryEntity getCategory(Long categoryId, Long storeId) {
        if (categoryId == null || categoryId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<MerchantCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantCategoryEntity::getId, categoryId)
                .eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .last("LIMIT 1");
        MerchantCategoryEntity category = merchantCategoryMapper.selectOne(queryWrapper);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
        return category;
    }

    private MerchantProductEntity getProduct(Long id, Long storeId) {
        LambdaQueryWrapper<MerchantProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantProductEntity::getId, id)
                .eq(MerchantProductEntity::getStoreId, storeId)
                .eq(MerchantProductEntity::getIsDeleted, 0)
                .last("LIMIT 1");
        MerchantProductEntity product = merchantProductMapper.selectOne(queryWrapper);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        return product;
    }

    private ProductVO toProductVO(MerchantProductEntity product, String categoryName) {
        ProductVO productVO = new ProductVO();
        productVO.setProductId(product.getId());
        productVO.setStoreId(product.getStoreId());
        productVO.setCategoryId(product.getCategoryId());
        productVO.setCategoryName(categoryName);
        productVO.setProductName(product.getProductName());
        productVO.setProductImage(product.getProductImage());
        productVO.setProductDescription(product.getProductDescription());
        productVO.setPrice(product.getPrice());
        productVO.setOriginalPrice(product.getOriginalPrice());
        productVO.setStock(product.getStock());
        productVO.setSales(product.getSales());
        productVO.setProductStatus(product.getProductStatus());
        productVO.setProductStatusText(ProductStatusEnum.getTextByCode(product.getProductStatus()));
        productVO.setSort(product.getSort());
        productVO.setCreateTime(product.getCreateTime());
        productVO.setUpdateTime(product.getUpdateTime());
        return productVO;
    }

    private PageResultVO<ProductVO> buildPageResult(Page<ProductVO> page, List<ProductVO> records,
                                                     long pageNum, long pageSize) {
        PageResultVO<ProductVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(pageNum);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1L : pageNum.longValue();
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize.longValue(), 50L);
    }

    private int defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
