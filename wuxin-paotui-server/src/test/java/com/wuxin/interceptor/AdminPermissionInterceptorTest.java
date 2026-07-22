package com.wuxin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuxin.annotation.AdminPermission;
import com.wuxin.service.AdminPermissionService;
import com.wuxin.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminPermissionInterceptorTest {

    private final AdminPermissionService adminPermissionService =
            mock(AdminPermissionService.class);

    private final AdminPermissionInterceptor interceptor =
            new AdminPermissionInterceptor(
                    adminPermissionService,
                    new ObjectMapper());

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void ordinaryUserShouldReceiveForbidden() throws Exception {
        UserContext.setUserId(2L);
        when(adminPermissionService.isAdmin(2L)).thenReturn(false);
        MockHttpServletResponse response =
                new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest(),
                response,
                new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
                .contains("\"code\":403")
                .contains("无管理员权限");
    }

    @Test
    void administratorShouldBeAllowed() throws Exception {
        UserContext.setUserId(1L);
        when(adminPermissionService.isAdmin(1L)).thenReturn(true);

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void administratorWithoutRequiredPermissionShouldReceiveForbidden() throws Exception {
        UserContext.setUserId(1L);
        when(adminPermissionService.isAdmin(1L)).thenReturn(true);
        when(adminPermissionService.hasPermission(1L, "order:view")).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest(), response, securedHandler());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void administratorWithRequiredPermissionShouldBeAllowed() throws Exception {
        UserContext.setUserId(1L);
        when(adminPermissionService.isAdmin(1L)).thenReturn(true);
        when(adminPermissionService.hasPermission(1L, "order:view")).thenReturn(true);

        boolean allowed = interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), securedHandler());

        assertThat(allowed).isTrue();
    }

    private HandlerMethod securedHandler() throws NoSuchMethodException {
        return new HandlerMethod(
                new SecuredController(), SecuredController.class.getDeclaredMethod("orders"));
    }

    private static class SecuredController {
        @AdminPermission("order:view")
        public void orders() {
        }
    }
}
