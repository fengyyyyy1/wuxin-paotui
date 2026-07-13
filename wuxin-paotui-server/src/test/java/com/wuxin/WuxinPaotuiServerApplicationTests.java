package com.wuxin;

import com.wuxin.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WuxinPaotuiServerApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Test
	void contextLoads() {
		assertThat(userMapper).isNotNull();
		assertThat(userMapper.selectCount(null)).isNotNegative();
	}

}
