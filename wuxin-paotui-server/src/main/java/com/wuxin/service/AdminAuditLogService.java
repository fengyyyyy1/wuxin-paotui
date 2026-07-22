package com.wuxin.service;

public interface AdminAuditLogService {
    void recordForUser(
            Long adminUserId,
            String moduleCode,
            String operationCode,
            String operationName,
            String targetType,
            Object targetId,
            Object beforeData,
            Object afterData);

    void record(
            String moduleCode,
            String operationCode,
            String operationName,
            String targetType,
            Object targetId,
            Object beforeData,
            Object afterData);
}
