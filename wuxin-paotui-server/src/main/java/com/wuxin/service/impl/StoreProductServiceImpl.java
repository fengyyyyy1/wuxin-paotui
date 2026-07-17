package com.wuxin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantCategoryMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.service.StoreProductService;
import com.wuxin.service.StoreService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.PublicCategoryVO;
import com.wuxin.vo.PublicProductVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreProductServiceImpl implements StoreProductService {

    private final StoreService storeService;

    private final MerchantCategoryMapper merchantCategoryMapper;

    private final MerchantProductMapper merchantProductMapper;

    public StoreProductServiceImpl(StoreService storeService,
                                   MerchantCategoryMapper merchantCategoryMapper,
                                   MerchantProductMapper merchantProductMapper) {
        this.storeService = storeService;
        this.merchantCategoryMapper = merchantCategoryMapper;
        this.merchantProductMapper = merchantProductMapper;
    }

    @Override
    public List<PublicCategoryVO> getCategoryList(Long storeId) {
        validateId(storeId);
        storeService.getStoreDetail(storeId);
        return merchantCategoryMapper.selectPublicList(storeId);
    }

    @Override
    public PageResultVO<PublicProductVO> getProductList(Long storeId, Integer pageNum, Integer pageSize,
                                                        Long categoryId, String keyword) {
        validateId(storeId);
        if (categoryId != null && categoryId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        storeService.getStoreDetail(storeId);

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);
        Page<PublicProductVO> page = merchantProductMapper.selectPublicPage(
                new Page<>(safePageNum, safePageSize), storeId, categoryId, normalize(keyword));

        PageResultVO<PublicProductVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(page.getRecords());
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    public PublicProductVO getProductDetail(Long id) {
        validateId(id);
        PublicProductVO product = merchantProductMapper.selectPublicDetail(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        return product;
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
