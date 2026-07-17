package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.merchant.CreateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryStatusDTO;
import com.wuxin.entity.MerchantCategoryEntity;
import com.wuxin.entity.MerchantProductEntity;
import com.wuxin.enums.CategoryStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantCategoryMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.service.MerchantCategoryService;
import com.wuxin.service.MerchantService;
import com.wuxin.vo.CategoryVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantCategoryServiceImpl implements MerchantCategoryService {

    private final MerchantService merchantService;

    private final MerchantCategoryMapper merchantCategoryMapper;

    private final MerchantProductMapper merchantProductMapper;

    public MerchantCategoryServiceImpl(MerchantService merchantService,
                                       MerchantCategoryMapper merchantCategoryMapper,
                                       MerchantProductMapper merchantProductMapper) {
        this.merchantService = merchantService;
        this.merchantCategoryMapper = merchantCategoryMapper;
        this.merchantProductMapper = merchantProductMapper;
    }

    @Override
    public CategoryVO createCategory(CreateCategoryDTO createCategoryDTO) {
        Long storeId = merchantService.getCurrentApprovedStoreId();
        String categoryName = createCategoryDTO.getCategoryName().trim();
        ensureCategoryNameAvailable(storeId, categoryName, null);

        LocalDateTime now = LocalDateTime.now();
        MerchantCategoryEntity category = new MerchantCategoryEntity();
        category.setStoreId(storeId);
        category.setCategoryName(categoryName);
        category.setSort(defaultSort(createCategoryDTO.getSort()));
        category.setStatus(CategoryStatusEnum.ENABLED.getCode());
        category.setCreateTime(now);
        category.setUpdateTime(now);
        category.setIsDeleted(0);

        try {
            if (merchantCategoryMapper.insert(category) != 1 || category.getId() == null) {
                throw new IllegalStateException("merchant category save failed");
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_EXIST);
        }
        return toCategoryVO(category);
    }

    @Override
    public void updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        getCategory(id, storeId);

        String categoryName = updateCategoryDTO.getCategoryName().trim();
        ensureCategoryNameAvailable(storeId, categoryName, id);

        LambdaUpdateWrapper<MerchantCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantCategoryEntity::getId, id)
                .eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .set(MerchantCategoryEntity::getCategoryName, categoryName)
                .set(MerchantCategoryEntity::getSort, defaultSort(updateCategoryDTO.getSort()))
                .set(MerchantCategoryEntity::getUpdateTime, LocalDateTime.now());
        try {
            if (merchantCategoryMapper.update(null, updateWrapper) != 1) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_EXIST);
        }
    }

    @Override
    public void updateCategoryStatus(Long id, UpdateCategoryStatusDTO updateCategoryStatusDTO) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        getCategory(id, storeId);
        if (CategoryStatusEnum.of(updateCategoryStatusDTO.getStatus()) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        LambdaUpdateWrapper<MerchantCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantCategoryEntity::getId, id)
                .eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .set(MerchantCategoryEntity::getStatus, updateCategoryStatusDTO.getStatus())
                .set(MerchantCategoryEntity::getUpdateTime, LocalDateTime.now());
        if (merchantCategoryMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
    }

    @Override
    public void deleteCategory(Long id) {
        validateId(id);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        getCategory(id, storeId);

        LambdaQueryWrapper<MerchantProductEntity> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(MerchantProductEntity::getStoreId, storeId)
                .eq(MerchantProductEntity::getCategoryId, id)
                .eq(MerchantProductEntity::getIsDeleted, 0);
        if (merchantProductMapper.selectCount(productQuery) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_PRODUCT);
        }

        LambdaUpdateWrapper<MerchantCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantCategoryEntity::getId, id)
                .eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .set(MerchantCategoryEntity::getIsDeleted, 1)
                .set(MerchantCategoryEntity::getUpdateTime, LocalDateTime.now());
        if (merchantCategoryMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
    }

    @Override
    public List<CategoryVO> getCategoryList() {
        Long storeId = merchantService.getCurrentApprovedStoreId();
        List<CategoryVO> categories = merchantCategoryMapper.selectManagementList(storeId);
        categories.forEach(category -> category.setStatusText(
                CategoryStatusEnum.getTextByCode(category.getStatus())));
        return categories;
    }

    private MerchantCategoryEntity getCategory(Long id, Long storeId) {
        LambdaQueryWrapper<MerchantCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantCategoryEntity::getId, id)
                .eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .last("LIMIT 1");
        MerchantCategoryEntity category = merchantCategoryMapper.selectOne(queryWrapper);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
        return category;
    }

    private void ensureCategoryNameAvailable(Long storeId, String categoryName, Long excludedId) {
        LambdaQueryWrapper<MerchantCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantCategoryEntity::getStoreId, storeId)
                .eq(MerchantCategoryEntity::getCategoryName, categoryName)
                .eq(MerchantCategoryEntity::getIsDeleted, 0)
                .ne(excludedId != null, MerchantCategoryEntity::getId, excludedId);
        if (merchantCategoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.CATEGORY_NAME_EXIST);
        }
    }

    private CategoryVO toCategoryVO(MerchantCategoryEntity category) {
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setCategoryId(category.getId());
        categoryVO.setCategoryName(category.getCategoryName());
        categoryVO.setSort(category.getSort());
        categoryVO.setStatus(category.getStatus());
        categoryVO.setStatusText(CategoryStatusEnum.getTextByCode(category.getStatus()));
        categoryVO.setCreateTime(category.getCreateTime());
        return categoryVO;
    }

    private int defaultSort(Integer sort) {
        return sort == null ? 0 : sort;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
