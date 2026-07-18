package com.wuxin.service;

import com.wuxin.dto.merchant.MerchantOrderPageQueryDTO;
import com.wuxin.dto.merchant.RejectMerchantOrderDTO;
import com.wuxin.vo.MerchantOrderDetailVO;
import com.wuxin.vo.MerchantOrderPageVO;
import com.wuxin.vo.MerchantOrderStatusVO;
import com.wuxin.vo.PageResultVO;

public interface MerchantOrderService {

    PageResultVO<MerchantOrderPageVO> pageOrders(MerchantOrderPageQueryDTO query);

    MerchantOrderDetailVO getOrderDetail(Long orderId);

    MerchantOrderStatusVO acceptOrder(Long orderId);

    MerchantOrderStatusVO rejectOrder(
            Long orderId,
            RejectMerchantOrderDTO request);

    MerchantOrderStatusVO readyOrder(Long orderId);
}
