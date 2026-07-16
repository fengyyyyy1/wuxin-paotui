package com.wuxin.service;

import com.wuxin.vo.HallOrderVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.AcceptOrderVO;
import com.wuxin.vo.FinishOrderVO;
import com.wuxin.vo.GiveUpOrderVO;
import com.wuxin.vo.RiderOrderVO;

public interface RiderOrderService {

    PageResultVO<HallOrderVO> getHallOrders(Integer pageNum, Integer pageSize);

    AcceptOrderVO acceptOrder(Long id);

    PageResultVO<RiderOrderVO> getMyOrders(Integer pageNum, Integer pageSize, Integer status);

    FinishOrderVO finishOrder(Long id);

    GiveUpOrderVO giveUpOrder(Long id);
}
