package com.wuxin.service;

import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.StoreDetailVO;
import com.wuxin.vo.StoreListVO;

public interface StoreService {

    PageResultVO<StoreListVO> getStoreList(Integer pageNum, Integer pageSize, String keyword,
                                           String district, Integer businessStatus);

    StoreDetailVO getStoreDetail(Long id);
}
