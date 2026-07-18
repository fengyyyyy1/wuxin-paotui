package com.wuxin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.service.AdminPermissionService;
import com.wuxin.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private final AdminPermissionService adminPermissionService;

    private final ObjectMapper objectMapper;

    public AdminPermissionInterceptor(
            AdminPermissionService adminPermissionService,
            ObjectMapper objectMapper) {
        this.adminPermissionService = adminPermissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            writeError(response, ResultCode.UNAUTHORIZED);
            return false;
        }
        if (!adminPermissionService.isAdmin(userId)) {
            writeError(response, ResultCode.ADMIN_FORBIDDEN);
            return false;
        }
        return true;
    }

    private void writeError(
            HttpServletResponse response,
            ResultCode resultCode) throws Exception {
        response.setStatus(resultCode.getCode());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.fail(resultCode)));
    }
}
