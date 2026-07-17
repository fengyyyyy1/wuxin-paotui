package com.wuxin.service;

import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.PublicCategoryVO;
import com.wuxin.vo.PublicProductVO;

import java.util.List;

public interface StoreProductService {

    List<PublicCategoryVO> getCategoryList(Long storeId);

    PageResultVO<PublicProductVO> getProductList(Long storeId, Integer pageNum, Integer pageSize,
                                                 Long categoryId, String keyword);

    PublicProductVO getProductDetail(Long id);
}
