package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.service.RiderOrderService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.AcceptOrderVO;
import com.wuxin.vo.FinishOrderVO;
import com.wuxin.vo.HallOrderVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.RiderOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiderOrderServiceImpl implements RiderOrderService {

    private static final Integer RIDER_AUDIT_APPROVED = 1;

    private static final Integer RIDER_ENABLED_STATUS = 1;

    private static final String OPERATOR_TYPE_RIDER = "RIDER";

    private static final String FINISH_STATUS_ERROR_MESSAGE = "\u5f53\u524d\u8ba2\u5355\u72b6\u6001\u4e0d\u53ef\u5b8c\u6210\u914d\u9001";

    private final OrderMapper orderMapper;

    private final RiderInfoMapper riderInfoMapper;

    private final OrderLogMapper orderLogMapper;

    public RiderOrderServiceImpl(OrderMapper orderMapper, RiderInfoMapper riderInfoMapper, OrderLogMapper orderLogMapper) {
        this.orderMapper = orderMapper;
        this.riderInfoMapper = riderInfoMapper;
        this.orderLogMapper = orderLogMapper;
    }

    @Override
    public PageResultVO<HallOrderVO> getHallOrders(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_ACCEPT.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = orderMapper.selectPage(new Page<>(safePageNum, safePageSize), queryWrapper);
        List<HallOrderVO> records = page.getRecords().stream()
                .map(this::toHallOrderVO)
                .toList();

        PageResultVO<HallOrderVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AcceptOrderVO acceptOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_ACCEPT.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getRiderId, riderInfo.getId())
                .set(OrderEntity::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                .set(OrderEntity::getAcceptTime, now)
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = orderMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            handleAcceptFailed(id);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setNewStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_RIDER);
        orderLog.setRemark("\u9a91\u624b\u63a5\u5355");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new BusinessException(ResultCode.FAIL, "order log save failed");
        }

        AcceptOrderVO acceptOrderVO = new AcceptOrderVO();
        acceptOrderVO.setOrderId(id);
        acceptOrderVO.setStatus(OrderStatusEnum.ACCEPTED.getCode());
        acceptOrderVO.setStatusText(OrderStatusEnum.ACCEPTED.getText());
        acceptOrderVO.setAcceptTime(now);
        return acceptOrderVO;
    }

    @Override
    public PageResultVO<RiderOrderVO> getMyOrders(Integer pageNum, Integer pageSize, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getRiderId, riderInfo.getId())
                .eq(OrderEntity::getDeleted, 0)
                .eq(status != null, OrderEntity::getStatus, status)
                .orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = orderMapper.selectPage(new Page<>(safePageNum, safePageSize), queryWrapper);
        List<RiderOrderVO> records = page.getRecords().stream()
                .map(this::toRiderOrderVO)
                .toList();

        PageResultVO<RiderOrderVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinishOrderVO finishOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        OrderEntity orderEntity = getRiderOrder(id, riderInfo.getId());
        if (!OrderStatusEnum.ACCEPTED.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, FINISH_STATUS_ERROR_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderInfo.getId())
                .eq(OrderEntity::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(OrderEntity::getFinishTime, now)
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = orderMapper.update(null, updateWrapper);
        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, FINISH_STATUS_ERROR_MESSAGE);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderLog.setNewStatus(OrderStatusEnum.COMPLETED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_RIDER);
        orderLog.setRemark("\u9a91\u624b\u5b8c\u6210\u914d\u9001");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new BusinessException(ResultCode.FAIL, "order log save failed");
        }

        FinishOrderVO finishOrderVO = new FinishOrderVO();
        finishOrderVO.setOrderId(id);
        finishOrderVO.setStatus(OrderStatusEnum.COMPLETED.getCode());
        finishOrderVO.setStatusText(OrderStatusEnum.COMPLETED.getText());
        finishOrderVO.setFinishTime(now);
        return finishOrderVO;
    }

    private HallOrderVO toHallOrderVO(OrderEntity orderEntity) {
        HallOrderVO hallOrderVO = new HallOrderVO();
        hallOrderVO.setId(orderEntity.getId());
        hallOrderVO.setOrderNo(orderEntity.getOrderNo());
        hallOrderVO.setGoodsName(orderEntity.getGoodsName());
        hallOrderVO.setGoodsDescription(orderEntity.getGoodsDescription());
        hallOrderVO.setWeight(orderEntity.getWeight());
        hallOrderVO.setDistance(orderEntity.getDistance());
        hallOrderVO.setPrice(orderEntity.getPrice());
        hallOrderVO.setPickupAddressId(orderEntity.getPickupAddressId());
        hallOrderVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        hallOrderVO.setStatus(orderEntity.getStatus());
        hallOrderVO.setStatusText(OrderStatusEnum.getTextByCode(orderEntity.getStatus()));
        hallOrderVO.setCreateTime(orderEntity.getCreateTime());
        return hallOrderVO;
    }

    private RiderOrderVO toRiderOrderVO(OrderEntity orderEntity) {
        RiderOrderVO riderOrderVO = new RiderOrderVO();
        riderOrderVO.setId(orderEntity.getId());
        riderOrderVO.setOrderNo(orderEntity.getOrderNo());
        riderOrderVO.setGoodsName(orderEntity.getGoodsName());
        riderOrderVO.setGoodsDescription(orderEntity.getGoodsDescription());
        riderOrderVO.setWeight(orderEntity.getWeight());
        riderOrderVO.setDistance(orderEntity.getDistance());
        riderOrderVO.setPrice(orderEntity.getPrice());
        riderOrderVO.setPickupAddressId(orderEntity.getPickupAddressId());
        riderOrderVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        riderOrderVO.setStatus(orderEntity.getStatus());
        riderOrderVO.setStatusText(OrderStatusEnum.getTextByCode(orderEntity.getStatus()));
        riderOrderVO.setAcceptTime(orderEntity.getAcceptTime());
        riderOrderVO.setFinishTime(orderEntity.getFinishTime());
        riderOrderVO.setCreateTime(orderEntity.getCreateTime());
        return riderOrderVO;
    }

    private OrderEntity getRiderOrder(Long id, Long riderId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = orderMapper.selectOne(queryWrapper);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return orderEntity;
    }

    private RiderInfoEntity getCurrentRider(Long userId) {
        LambdaQueryWrapper<RiderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RiderInfoEntity::getUserId, userId)
                .last("LIMIT 1");

        RiderInfoEntity riderInfo = riderInfoMapper.selectOne(queryWrapper);
        if (riderInfo == null
                || !RIDER_AUDIT_APPROVED.equals(riderInfo.getAuditStatus())
                || !RIDER_ENABLED_STATUS.equals(riderInfo.getRiderStatus())) {
            throw new BusinessException(ResultCode.NOT_RIDER);
        }
        return riderInfo;
    }

    private void handleAcceptFailed(Long id) {
        OrderEntity orderEntity = orderMapper.selectById(id);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (OrderStatusEnum.CANCELLED.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
    }

    private long normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return 1L;
        }
        return pageNum.longValue();
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize.longValue(), 50L);
    }
}
