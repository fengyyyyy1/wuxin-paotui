package com.wuxin.service;

import com.wuxin.dto.merchant.CreateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryDTO;
import com.wuxin.dto.merchant.UpdateCategoryStatusDTO;
import com.wuxin.vo.CategoryVO;

import java.util.List;

public interface MerchantCategoryService {

    CategoryVO createCategory(CreateCategoryDTO createCategoryDTO);

    void updateCategory(Long id, UpdateCategoryDTO updateCategoryDTO);

    void updateCategoryStatus(Long id, UpdateCategoryStatusDTO updateCategoryStatusDTO);

    void deleteCategory(Long id);

    List<CategoryVO> getCategoryList();
}
