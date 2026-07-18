package com.wuxin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtInterceptorTest {

    @Test
    void phoneBindingShouldRejectUnauthenticatedRequest() throws Exception {
        JwtInterceptor interceptor = new JwtInterceptor(new ObjectMapper());
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/api/user/phone/bind");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("\"code\":401")
                .contains("未登录或登录已过期");
    }
}
