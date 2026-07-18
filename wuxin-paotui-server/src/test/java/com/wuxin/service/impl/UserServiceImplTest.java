package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.wechat.BindWechatPhoneDTO;
import com.wuxin.dto.wechat.WeChatLoginDTO;
import com.wuxin.entity.UserEntity;
import com.wuxin.exception.BusinessException;
import com.wuxin.gateway.WeChatMiniProgramGatewayRouter;
import com.wuxin.gateway.WeChatPhoneGateway;
import com.wuxin.gateway.WeChatPhoneGatewayRouter;
import com.wuxin.gateway.impl.MockWeChatMiniProgramGateway;
import com.wuxin.gateway.model.WeChatPhoneResult;
import com.wuxin.mapper.UserMapper;
import com.wuxin.vo.UserInfoVO;
import com.wuxin.vo.WeChatLoginVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @BeforeAll
    static void initializeMyBatisPlusMetadata() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void mockWeChatFirstAndRepeatedLoginShouldCreateOnlyOneUser() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatMiniProgramGatewayRouter gatewayRouter =
                mock(WeChatMiniProgramGatewayRouter.class);
        when(gatewayRouter.getActiveGateway())
                .thenReturn(new MockWeChatMiniProgramGateway());

        PlatformTransactionManager transactionManager =
                mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(mock(TransactionStatus.class));
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        AtomicReference<UserEntity> storedUser = new AtomicReference<>();
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> storedUser.get());
        when(userMapper.countByUsernameIncludingDeleted(anyString())).thenReturn(0L);
        when(userMapper.insert(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(7L);
            storedUser.set(user);
            return 1;
        });

        UserServiceImpl service = new UserServiceImpl(
                gatewayRouter,
                mock(WeChatPhoneGatewayRouter.class),
                transactionTemplate);
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);

        WeChatLoginDTO request = new WeChatLoginDTO();
        request.setCode("mock-code-new-user");

        WeChatLoginVO firstLogin = service.wechatLogin(request);
        WeChatLoginVO repeatedLogin = service.wechatLogin(request);

        assertThat(firstLogin.getNewUser()).isTrue();
        assertThat(firstLogin.getToken()).isNotBlank();
        assertThat(firstLogin.getUserInfo().getId()).isEqualTo(7L);
        assertThat(repeatedLogin.getNewUser()).isFalse();
        assertThat(repeatedLogin.getUserInfo().getId()).isEqualTo(7L);
        assertThat(storedUser.get().getPassword())
                .startsWith("$2")
                .hasSize(60)
                .isNotEqualTo("123456");
        verify(userMapper, times(1)).insert(any(UserEntity.class));
    }

    @Test
    void generatedWeChatPasswordShouldBeUniqueAndWithinBcryptByteLimit() {
        UserServiceImpl service = new UserServiceImpl(
                mock(WeChatMiniProgramGatewayRouter.class),
                mock(WeChatPhoneGatewayRouter.class),
                new TransactionTemplate(mock(PlatformTransactionManager.class)));
        Set<String> generatedPasswords = new HashSet<>();

        for (int index = 0; index < 100; index++) {
            String password = service.generateWeChatRandomPassword();
            assertThat(password.getBytes(StandardCharsets.UTF_8).length)
                    .isEqualTo(36)
                    .isLessThanOrEqualTo(72);
            generatedPasswords.add(password);
        }

        assertThat(generatedPasswords).hasSize(100);
    }

    @Test
    void bindWeChatPhoneShouldSupportFirstBindAndKeepOtherFieldsUnchanged() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        WeChatPhoneGateway phoneGateway = mock(WeChatPhoneGateway.class);
        when(phoneGatewayRouter.getActiveGateway()).thenReturn(phoneGateway);
        when(phoneGateway.exchangeCode("mock-phone-code-13800000003"))
                .thenReturn(phoneResult("13800000003"));

        UserEntity user = activeUser(null);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(userMapper.countActiveUsersByPhoneExcludingUser("13800000003", 3L))
                .thenReturn(0L);
        when(userMapper.update(
                eq(null),
                any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserServiceImpl service = newUserService(userMapper, phoneGatewayRouter);
        BindWechatPhoneDTO request = phoneRequest("  mock-phone-code-13800000003  ");

        UserInfoVO result = service.bindWechatPhone(3L, request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getPhone()).isEqualTo("13800000003");
        assertThat(user.getUsername()).isEqualTo("wx_user");
        assertThat(user.getOpenId()).isEqualTo("mock_openid_new_user");
        assertThat(user.getUnionId()).isEqualTo("mock_unionid_new_user");
        assertThat(user.getPassword()).isEqualTo("$2a$10$existingHash");
        assertThat(user.getStatus()).isEqualTo(1);
        verify(phoneGateway).exchangeCode("mock-phone-code-13800000003");
        verify(userMapper).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void bindWeChatPhoneShouldBeIdempotentForSamePhone() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        WeChatPhoneGateway phoneGateway = mock(WeChatPhoneGateway.class);
        when(phoneGatewayRouter.getActiveGateway()).thenReturn(phoneGateway);
        when(phoneGateway.exchangeCode(anyString()))
                .thenReturn(phoneResult("13800000003"));

        UserEntity user = activeUser("13800000003");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(userMapper.countActiveUsersByPhoneExcludingUser("13800000003", 3L))
                .thenReturn(0L);

        UserInfoVO result = newUserService(userMapper, phoneGatewayRouter)
                .bindWechatPhone(3L, phoneRequest("mock-phone-code-13800000003"));

        assertThat(result.getPhone()).isEqualTo("13800000003");
        verify(userMapper, never()).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void bindWeChatPhoneShouldAllowReplacingWithUnoccupiedPhone() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        WeChatPhoneGateway phoneGateway = mock(WeChatPhoneGateway.class);
        when(phoneGatewayRouter.getActiveGateway()).thenReturn(phoneGateway);
        when(phoneGateway.exchangeCode(anyString()))
                .thenReturn(phoneResult("13900000003"));

        UserEntity user = activeUser("13800000003");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(userMapper.countActiveUsersByPhoneExcludingUser("13900000003", 3L))
                .thenReturn(0L);
        when(userMapper.update(eq(null), any(LambdaUpdateWrapper.class))).thenReturn(1);

        UserInfoVO result = newUserService(userMapper, phoneGatewayRouter)
                .bindWechatPhone(3L, phoneRequest("mock-phone-code-13900000003"));

        assertThat(result.getPhone()).isEqualTo("13900000003");
    }

    @Test
    void bindWeChatPhoneShouldRejectPhoneOwnedByAnotherUser() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        WeChatPhoneGateway phoneGateway = mock(WeChatPhoneGateway.class);
        when(phoneGatewayRouter.getActiveGateway()).thenReturn(phoneGateway);
        when(phoneGateway.exchangeCode(anyString()))
                .thenReturn(phoneResult("13800000003"));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(activeUser(null));
        when(userMapper.countActiveUsersByPhoneExcludingUser("13800000003", 3L))
                .thenReturn(1L);

        assertThatThrownBy(() -> newUserService(userMapper, phoneGatewayRouter)
                .bindWechatPhone(3L, phoneRequest("mock-phone-code-13800000003")))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.PHONE_ALREADY_BOUND));
        verify(userMapper, never()).update(eq(null), any(LambdaUpdateWrapper.class));
    }

    @Test
    void bindWeChatPhoneShouldReturnBusinessErrorForInvalidCode() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        WeChatPhoneGateway phoneGateway = mock(WeChatPhoneGateway.class);
        when(phoneGatewayRouter.getActiveGateway()).thenReturn(phoneGateway);
        when(phoneGateway.exchangeCode(anyString()))
                .thenThrow(new BusinessException(ResultCode.WECHAT_PHONE_CODE_INVALID));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(activeUser(null));

        assertThatThrownBy(() -> newUserService(userMapper, phoneGatewayRouter)
                .bindWechatPhone(3L, phoneRequest("mock-phone-code-invalid")))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo(400));
    }

    @Test
    void bindWeChatPhoneShouldReturnBusinessErrorWhenMockIsDisabled() {
        UserMapper userMapper = mock(UserMapper.class);
        WeChatPhoneGatewayRouter phoneGatewayRouter = mock(WeChatPhoneGatewayRouter.class);
        when(phoneGatewayRouter.getActiveGateway())
                .thenThrow(new BusinessException(ResultCode.WECHAT_PHONE_BIND_DISABLED));
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(activeUser(null));

        assertThatThrownBy(() -> newUserService(userMapper, phoneGatewayRouter)
                .bindWechatPhone(3L, phoneRequest("mock-phone-code-13800000003")))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo(503));
    }

    private UserServiceImpl newUserService(
            UserMapper userMapper,
            WeChatPhoneGatewayRouter phoneGatewayRouter) {
        UserServiceImpl service = new UserServiceImpl(
                mock(WeChatMiniProgramGatewayRouter.class),
                phoneGatewayRouter,
                new TransactionTemplate(mock(PlatformTransactionManager.class)));
        ReflectionTestUtils.setField(service, "baseMapper", userMapper);
        return service;
    }

    private UserEntity activeUser(String phone) {
        UserEntity user = new UserEntity();
        user.setId(3L);
        user.setUsername("wx_user");
        user.setPassword("$2a$10$existingHash");
        user.setOpenId("mock_openid_new_user");
        user.setUnionId("mock_unionid_new_user");
        user.setNickname("悠悠球");
        user.setAvatar("avatar");
        user.setPhone(phone);
        user.setGender(0);
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }

    private BindWechatPhoneDTO phoneRequest(String code) {
        BindWechatPhoneDTO request = new BindWechatPhoneDTO();
        request.setCode(code);
        return request;
    }

    private WeChatPhoneResult phoneResult(String phone) {
        return WeChatPhoneResult.builder()
                .phoneNumber("+86" + phone)
                .purePhoneNumber(phone)
                .countryCode("86")
                .build();
    }
}
