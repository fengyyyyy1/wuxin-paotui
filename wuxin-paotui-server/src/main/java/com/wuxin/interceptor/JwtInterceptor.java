package com.wuxin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxin.common.Result;
import com.wuxin.common.ResultCode;
import com.wuxin.utils.JwtUtils;
import com.wuxin.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    public JwtInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicStoreQuery(request)) {
            return true;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response);
            return false;
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (!JwtUtils.validateToken(token)) {
            writeUnauthorized(response);
            return false;
        }

        UserContext.setUserId(JwtUtils.getUserId(token));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED)));
    }

    private boolean isPublicStoreQuery(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return "/api/store/list".equals(requestPath)
                || requestPath.matches("^/api/store/\\d+$")
                || requestPath.matches("^/api/store/\\d+/categories$")
                || requestPath.matches("^/api/store/\\d+/products$")
                || requestPath.matches("^/api/store/product/\\d+$");
    }
}
