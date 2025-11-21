package com.supernovapos.finalproject.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AppConfigTest {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void testEncode() {
	    System.out.println(passwordEncoder.encode("123456"));
	}

}
