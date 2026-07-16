package com.wuxin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.service.StoreService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.StoreDetailVO;
import com.wuxin.vo.StoreListVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    private final MerchantStoreMapper merchantStoreMapper;

    public StoreServiceImpl(MerchantStoreMapper merchantStoreMapper) {
        this.merchantStoreMapper = merchantStoreMapper;
    }

    @Override
    public PageResultVO<StoreListVO> getStoreList(Integer pageNum, Integer pageSize, String keyword,
                                                  String district, Integer businessStatus) {
        if (businessStatus != null && BusinessStatusEnum.of(businessStatus) == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);
        Page<StoreListVO> page = merchantStoreMapper.selectStorePage(
                new Page<>(safePageNum, safePageSize), normalize(keyword), normalize(district), businessStatus);

        List<StoreListVO> records = page.getRecords();
        records.forEach(store -> store.setBusinessStatusText(
                BusinessStatusEnum.getTextByCode(store.getBusinessStatus())));

        PageResultVO<StoreListVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    public StoreDetailVO getStoreDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        StoreDetailVO storeDetailVO = merchantStoreMapper.selectStoreDetail(id);
        if (storeDetailVO == null) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        storeDetailVO.setBusinessStatusText(
                BusinessStatusEnum.getTextByCode(storeDetailVO.getBusinessStatus()));
        return storeDetailVO;
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
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
