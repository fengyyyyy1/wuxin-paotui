package com.wuxin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxin.entity.AdminOperationLogEntity;
import com.wuxin.mapper.AdminOperationLogMapper;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminOperationLogMapper mapper;
    private final ObjectMapper objectMapper;

    public AdminAuditLogServiceImpl(AdminOperationLogMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(
            String moduleCode,
            String operationCode,
            String operationName,
            String targetType,
            Object targetId,
            Object beforeData,
            Object afterData) {
        recordForUser(UserContext.getUserId(), moduleCode, operationCode, operationName,
                targetType, targetId, beforeData, afterData);
    }

    @Override
    public void recordForUser(
            Long adminUserId,
            String moduleCode,
            String operationCode,
            String operationName,
            String targetType,
            Object targetId,
            Object beforeData,
            Object afterData) {
        if (adminUserId == null) {
            return;
        }
        HttpServletRequest request = currentRequest();
        AdminOperationLogEntity log = new AdminOperationLogEntity();
        log.setAdminUserId(adminUserId);
        log.setModuleCode(moduleCode);
        log.setOperationCode(operationCode);
        log.setOperationName(operationName);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? null : String.valueOf(targetId));
        log.setRequestMethod(request == null ? null : request.getMethod());
        log.setRequestPath(request == null ? null : request.getRequestURI());
        log.setRequestIp(request == null ? null : resolveIp(request));
        log.setBeforeData(toJson(beforeData));
        log.setAfterData(toJson(afterData));
        log.setResultStatus(1);
        log.setCreateTime(LocalDateTime.now());
        mapper.insert(log);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ignored) {
            return "{\"serializationError\":true}";
        }
    }
}
