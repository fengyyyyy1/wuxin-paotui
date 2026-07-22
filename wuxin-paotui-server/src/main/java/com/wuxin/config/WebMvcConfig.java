package com.wuxin.config;

import com.wuxin.interceptor.AdminPermissionInterceptor;
import com.wuxin.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    private final AdminPermissionInterceptor adminPermissionInterceptor;

    public WebMvcConfig(
            JwtInterceptor jwtInterceptor,
            AdminPermissionInterceptor adminPermissionInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
        this.adminPermissionInterceptor = adminPermissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .order(0)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/wechat/login",
                        "/api/user/register",
                        "/api/platform/home",
                        "/error"
                );
        registry.addInterceptor(adminPermissionInterceptor)
                .order(1)
                .addPathPatterns("/api/admin/**");
    }
}
