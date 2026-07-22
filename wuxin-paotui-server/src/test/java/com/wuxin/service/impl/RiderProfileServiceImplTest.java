package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.wuxin.dto.rider.RiderApplyDTO;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.RiderProfileVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RiderProfileServiceImplTest {

    private RiderInfoMapper riderInfoMapper;
    private UserMapper userMapper;
    private RiderProfileServiceImpl service;

    @BeforeAll
    static void initializeMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, RiderInfoEntity.class);
    }

    @BeforeEach
    void setUp() {
        riderInfoMapper = mock(RiderInfoMapper.class);
        userMapper = mock(UserMapper.class);
        service = new RiderProfileServiceImpl(riderInfoMapper, userMapper);
        UserContext.setUserId(2L);
    }

    @AfterEach
    void tearDown() {
        UserContext.remove();
    }

    @Test
    void newApplicationShouldUseCurrentUserAndPendingStatus() {
        when(userMapper.selectById(2L)).thenReturn(activeUser());
        when(riderInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        doAnswer(invocation -> {
            RiderInfoEntity rider = invocation.getArgument(0);
            rider.setId(9L);
            return 1;
        }).when(riderInfoMapper).insert(any(RiderInfoEntity.class));

        var result = service.apply(validRequest());

        assertThat(result.getRiderId()).isEqualTo(9L);
        assertThat(result.getAuditStatus()).isZero();
        verify(riderInfoMapper).insert(any(RiderInfoEntity.class));
    }

    @Test
    void profileShouldMaskIdentityCardAndReturnRealStatus() {
        RiderInfoEntity rider = new RiderInfoEntity();
        rider.setId(1L);
        rider.setUserId(2L);
        rider.setRealName("测试骑手");
        rider.setIdCard("500000199001011234");
        rider.setAuditStatus(1);
        rider.setRiderStatus(1);
        when(riderInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(rider);
        when(userMapper.selectById(2L)).thenReturn(activeUser());

        RiderProfileVO result = service.getCurrentProfile();

        assertThat(result.getIdCardMasked()).startsWith("5000").endsWith("1234");
        assertThat(result.getIdCardMasked()).doesNotContain("19900101");
        assertThat(result.getAuditStatusText()).isEqualTo("审核通过");
    }

    private UserEntity activeUser() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        user.setUsername("test001");
        user.setPhone("13800000001");
        user.setStatus(1);
        user.setIsDeleted(0);
        return user;
    }

    private RiderApplyDTO validRequest() {
        RiderApplyDTO request = new RiderApplyDTO();
        request.setRealName("测试骑手");
        request.setIdCard("500000199001011234");
        request.setIdCardFront("https://cos.example/rider/front.jpg");
        request.setIdCardBack("https://cos.example/rider/back.jpg");
        return request;
    }
}
