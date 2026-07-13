package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.RiderOrderService;
import com.wuxin.vo.AcceptOrderVO;
import com.wuxin.vo.FinishOrderVO;
import com.wuxin.vo.HallOrderVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.RiderOrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider/order")
public class RiderOrderController {

    private final RiderOrderService riderOrderService;

    public RiderOrderController(RiderOrderService riderOrderService) {
        this.riderOrderService = riderOrderService;
    }

    @GetMapping("/hall")
    public Result<PageResultVO<HallOrderVO>> hall(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return Result.success(riderOrderService.getHallOrders(pageNum, pageSize));
    }

    @GetMapping("/my")
    public Result<PageResultVO<RiderOrderVO>> myOrders(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(riderOrderService.getMyOrders(pageNum, pageSize, status));
    }

    @PostMapping("/accept/{id}")
    public Result<AcceptOrderVO> accept(@PathVariable Long id) {
        return Result.success("\u63a5\u5355\u6210\u529f", riderOrderService.acceptOrder(id));
    }

    @PostMapping("/finish/{id}")
    public Result<FinishOrderVO> finish(@PathVariable Long id) {
        return Result.success(riderOrderService.finishOrder(id));
    }
}
