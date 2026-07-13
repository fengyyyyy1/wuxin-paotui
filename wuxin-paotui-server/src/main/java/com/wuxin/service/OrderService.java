package com.wuxin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wuxin.dto.order.CreateOrderDTO;
import com.wuxin.entity.OrderEntity;
import com.wuxin.vo.ConfirmOrderVO;
import com.wuxin.vo.OrderDetailVO;
import com.wuxin.vo.OrderListVO;
import com.wuxin.vo.PageResultVO;

public interface OrderService extends IService<OrderEntity> {

    Long createOrder(CreateOrderDTO createOrderDTO);

    PageResultVO<OrderListVO> getMyOrders(Integer pageNum, Integer pageSize, Integer status);

    OrderDetailVO getOrderDetail(Long id);

    ConfirmOrderVO confirmOrder(Long id);
}
