package com.wuxin.service;

import com.wuxin.dto.merchant.CreateProductDTO;
import com.wuxin.dto.merchant.UpdateProductDTO;
import com.wuxin.dto.merchant.UpdateProductStatusDTO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.ProductVO;

public interface MerchantProductService {

    ProductVO createProduct(CreateProductDTO createProductDTO);

    void updateProduct(Long id, UpdateProductDTO updateProductDTO);

    String updateProductStatus(Long id, UpdateProductStatusDTO updateProductStatusDTO);

    void deleteProduct(Long id);

    PageResultVO<ProductVO> getProductList(Integer pageNum, Integer pageSize, Long categoryId,
                                           Integer productStatus, String keyword);
}
