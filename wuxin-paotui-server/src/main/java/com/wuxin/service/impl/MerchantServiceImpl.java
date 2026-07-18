package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.merchant.MerchantApplyDTO;
import com.wuxin.dto.merchant.UpdateBusinessStatusDTO;
import com.wuxin.dto.merchant.UpdateMerchantStoreDTO;
import com.wuxin.entity.MerchantInfoEntity;
import com.wuxin.entity.MerchantStoreEntity;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.enums.MerchantAuditStatusEnum;
import com.wuxin.enums.MerchantStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantInfoMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.service.MerchantService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.MerchantApplyVO;
import com.wuxin.vo.MerchantDetailVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantInfoMapper merchantInfoMapper;

    private final MerchantStoreMapper merchantStoreMapper;

    public MerchantServiceImpl(MerchantInfoMapper merchantInfoMapper, MerchantStoreMapper merchantStoreMapper) {
        this.merchantInfoMapper = merchantInfoMapper;
        this.merchantStoreMapper = merchantStoreMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantApplyVO apply(MerchantApplyDTO merchantApplyDTO) {
        Long userId = getCurrentUserId();
        if (findMerchantByUserId(userId) != null) {
            throw new BusinessException(ResultCode.MERCHANT_ALREADY_APPLIED);
        }

        LocalDateTime now = LocalDateTime.now();
        MerchantInfoEntity merchantInfo = buildMerchantInfo(merchantApplyDTO, userId, now);
        MerchantStoreEntity merchantStore;
        try {
            int insertedMerchantRows = merchantInfoMapper.insert(merchantInfo);
            if (insertedMerchantRows != 1 || merchantInfo.getId() == null) {
                throw new IllegalStateException("merchant info save failed");
            }

            merchantStore = buildMerchantStore(merchantApplyDTO, merchantInfo.getId(), now);
            int insertedStoreRows = merchantStoreMapper.insert(merchantStore);
            if (insertedStoreRows != 1 || merchantStore.getId() == null) {
                throw new IllegalStateException("merchant store save failed");
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.MERCHANT_ALREADY_APPLIED);
        }

        MerchantApplyVO merchantApplyVO = new MerchantApplyVO();
        merchantApplyVO.setMerchantId(merchantInfo.getId());
        merchantApplyVO.setStoreId(merchantStore.getId());
        merchantApplyVO.setAuditStatus(MerchantAuditStatusEnum.PENDING.getCode());
        merchantApplyVO.setAuditStatusText(MerchantAuditStatusEnum.PENDING.getText());
        merchantApplyVO.setApplyTime(now);
        return merchantApplyVO;
    }

    @Override
    public MerchantDetailVO getCurrentMerchant() {
        MerchantInfoEntity merchantInfo = getCurrentMerchantInfo(false);
        MerchantStoreEntity merchantStore = getMerchantStore(merchantInfo.getId(), false);
        return toMerchantDetailVO(merchantInfo, merchantStore);
    }

    @Override
    public void updateStore(UpdateMerchantStoreDTO updateMerchantStoreDTO) {
        MerchantInfoEntity merchantInfo = getCurrentMerchantInfo(true);
        MerchantStoreEntity merchantStore = getMerchantStore(merchantInfo.getId(), true);

        LambdaUpdateWrapper<MerchantStoreEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantStoreEntity::getId, merchantStore.getId())
                .eq(MerchantStoreEntity::getMerchantId, merchantInfo.getId())
                .eq(MerchantStoreEntity::getStoreStatus, MerchantStatusEnum.ENABLED.getCode())
                .eq(MerchantStoreEntity::getIsDeleted, 0)
                .set(MerchantStoreEntity::getStoreName, updateMerchantStoreDTO.getStoreName())
                .set(MerchantStoreEntity::getStoreLogo, updateMerchantStoreDTO.getStoreLogo())
                .set(MerchantStoreEntity::getStoreDescription, updateMerchantStoreDTO.getStoreDescription())
                .set(MerchantStoreEntity::getStorePhone, updateMerchantStoreDTO.getStorePhone())
                .set(MerchantStoreEntity::getProvince, updateMerchantStoreDTO.getProvince())
                .set(MerchantStoreEntity::getCity, updateMerchantStoreDTO.getCity())
                .set(MerchantStoreEntity::getDistrict, updateMerchantStoreDTO.getDistrict())
                .set(MerchantStoreEntity::getDetailAddress, updateMerchantStoreDTO.getDetailAddress())
                .set(MerchantStoreEntity::getLatitude, updateMerchantStoreDTO.getLatitude())
                .set(MerchantStoreEntity::getLongitude, updateMerchantStoreDTO.getLongitude())
                .set(MerchantStoreEntity::getOpenTime, updateMerchantStoreDTO.getOpenTime())
                .set(MerchantStoreEntity::getCloseTime, updateMerchantStoreDTO.getCloseTime())
                .set(MerchantStoreEntity::getUpdateTime, LocalDateTime.now());

        if (merchantStoreMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
    }

    @Override
    public void updateBusinessStatus(UpdateBusinessStatusDTO updateBusinessStatusDTO) {
        MerchantInfoEntity merchantInfo = getCurrentMerchantInfo(true);
        MerchantStoreEntity merchantStore = getMerchantStore(merchantInfo.getId(), true);

        LambdaUpdateWrapper<MerchantStoreEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantStoreEntity::getId, merchantStore.getId())
                .eq(MerchantStoreEntity::getMerchantId, merchantInfo.getId())
                .eq(MerchantStoreEntity::getStoreStatus, MerchantStatusEnum.ENABLED.getCode())
                .eq(MerchantStoreEntity::getIsDeleted, 0)
                .set(MerchantStoreEntity::getBusinessStatus, updateBusinessStatusDTO.getBusinessStatus())
                .set(MerchantStoreEntity::getUpdateTime, LocalDateTime.now());

        if (merchantStoreMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
    }

    @Override
    public Long getCurrentApprovedStoreId() {
        MerchantInfoEntity merchantInfo = getCurrentMerchantInfo(true);
        return getMerchantStore(merchantInfo.getId(), true).getId();
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private MerchantInfoEntity getCurrentMerchantInfo(boolean requireApproved) {
        MerchantInfoEntity merchantInfo = findMerchantByUserId(getCurrentUserId());
        if (merchantInfo == null) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_EXIST);
        }
        if (requireApproved
                && (!MerchantAuditStatusEnum.APPROVED.getCode().equals(merchantInfo.getAuditStatus())
                || !MerchantStatusEnum.ENABLED.getCode().equals(merchantInfo.getMerchantStatus()))) {
            throw new BusinessException(ResultCode.MERCHANT_NOT_APPROVED);
        }
        return merchantInfo;
    }

    private MerchantInfoEntity findMerchantByUserId(Long userId) {
        LambdaQueryWrapper<MerchantInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantInfoEntity::getUserId, userId)
                .eq(MerchantInfoEntity::getIsDeleted, 0)
                .last("LIMIT 1");
        return merchantInfoMapper.selectOne(queryWrapper);
    }

    private MerchantStoreEntity getMerchantStore(Long merchantId, boolean requireEnabled) {
        LambdaQueryWrapper<MerchantStoreEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantStoreEntity::getMerchantId, merchantId)
                .eq(MerchantStoreEntity::getIsDeleted, 0)
                .eq(requireEnabled, MerchantStoreEntity::getStoreStatus, MerchantStatusEnum.ENABLED.getCode())
                .last("LIMIT 1");
        MerchantStoreEntity merchantStore = merchantStoreMapper.selectOne(queryWrapper);
        if (merchantStore == null) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        return merchantStore;
    }

    private MerchantInfoEntity buildMerchantInfo(MerchantApplyDTO dto, Long userId, LocalDateTime now) {
        MerchantInfoEntity merchantInfo = new MerchantInfoEntity();
        merchantInfo.setUserId(userId);
        merchantInfo.setMerchantName(dto.getMerchantName());
        merchantInfo.setContactName(dto.getContactName());
        merchantInfo.setContactPhone(dto.getContactPhone());
        merchantInfo.setBusinessLicense(dto.getBusinessLicense());
        merchantInfo.setIdCardFront(dto.getIdCardFront());
        merchantInfo.setIdCardBack(dto.getIdCardBack());
        merchantInfo.setAuditStatus(MerchantAuditStatusEnum.PENDING.getCode());
        merchantInfo.setAuditRemark(null);
        merchantInfo.setMerchantStatus(MerchantStatusEnum.ENABLED.getCode());
        merchantInfo.setCreateTime(now);
        merchantInfo.setUpdateTime(now);
        merchantInfo.setIsDeleted(0);
        return merchantInfo;
    }

    private MerchantStoreEntity buildMerchantStore(MerchantApplyDTO dto, Long merchantId, LocalDateTime now) {
        MerchantStoreEntity merchantStore = new MerchantStoreEntity();
        merchantStore.setMerchantId(merchantId);
        merchantStore.setStoreName(dto.getStoreName());
        merchantStore.setStoreLogo(dto.getStoreLogo());
        merchantStore.setStoreDescription(dto.getStoreDescription());
        merchantStore.setStorePhone(dto.getStorePhone());
        merchantStore.setProvince(dto.getProvince());
        merchantStore.setCity(dto.getCity());
        merchantStore.setDistrict(dto.getDistrict());
        merchantStore.setDetailAddress(dto.getDetailAddress());
        merchantStore.setLatitude(dto.getLatitude());
        merchantStore.setLongitude(dto.getLongitude());
        merchantStore.setBusinessStatus(BusinessStatusEnum.CLOSED.getCode());
        merchantStore.setOpenTime(dto.getOpenTime());
        merchantStore.setCloseTime(dto.getCloseTime());
        merchantStore.setStoreStatus(MerchantStatusEnum.ENABLED.getCode());
        merchantStore.setCreateTime(now);
        merchantStore.setUpdateTime(now);
        merchantStore.setIsDeleted(0);
        return merchantStore;
    }

    private MerchantDetailVO toMerchantDetailVO(MerchantInfoEntity merchantInfo, MerchantStoreEntity merchantStore) {
        MerchantDetailVO detailVO = new MerchantDetailVO();
        detailVO.setMerchantId(merchantInfo.getId());
        detailVO.setMerchantName(merchantInfo.getMerchantName());
        detailVO.setContactName(merchantInfo.getContactName());
        detailVO.setContactPhone(merchantInfo.getContactPhone());
        detailVO.setBusinessLicense(merchantInfo.getBusinessLicense());
        detailVO.setAuditStatus(merchantInfo.getAuditStatus());
        detailVO.setAuditStatusText(MerchantAuditStatusEnum.getTextByCode(merchantInfo.getAuditStatus()));
        detailVO.setAuditRemark(merchantInfo.getAuditRemark());
        detailVO.setRejectReason(merchantInfo.getRejectReason());
        detailVO.setAuditTime(merchantInfo.getAuditTime());
        detailVO.setMerchantStatus(merchantInfo.getMerchantStatus());
        detailVO.setMerchantStatusText(MerchantStatusEnum.getTextByCode(merchantInfo.getMerchantStatus()));
        detailVO.setStoreId(merchantStore.getId());
        detailVO.setStoreName(merchantStore.getStoreName());
        detailVO.setStoreLogo(merchantStore.getStoreLogo());
        detailVO.setStoreDescription(merchantStore.getStoreDescription());
        detailVO.setStorePhone(merchantStore.getStorePhone());
        detailVO.setProvince(merchantStore.getProvince());
        detailVO.setCity(merchantStore.getCity());
        detailVO.setDistrict(merchantStore.getDistrict());
        detailVO.setDetailAddress(merchantStore.getDetailAddress());
        detailVO.setLatitude(merchantStore.getLatitude());
        detailVO.setLongitude(merchantStore.getLongitude());
        detailVO.setBusinessStatus(merchantStore.getBusinessStatus());
        detailVO.setBusinessStatusText(BusinessStatusEnum.getTextByCode(merchantStore.getBusinessStatus()));
        detailVO.setOpenTime(merchantStore.getOpenTime());
        detailVO.setCloseTime(merchantStore.getCloseTime());
        detailVO.setStoreStatus(merchantStore.getStoreStatus());
        detailVO.setStoreStatusText(MerchantStatusEnum.getTextByCode(merchantStore.getStoreStatus()));
        detailVO.setCreateTime(merchantInfo.getCreateTime());
        detailVO.setUpdateTime(merchantInfo.getUpdateTime());
        return detailVO;
    }
}
