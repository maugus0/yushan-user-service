package com.yushan.user_service;

import com.yushan.user_service.service.MailService;
import com.yushan.user_service.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "jwt.secret=a-very-long-and-secure-secret-key-for-testing-purposes",
        "jwt.issuer=test-issuer",
        "jwt.access-token.expiration=3600000",
        "jwt.refresh-token.expiration=86400000",
        "jwt.algorithm=HS256",
        "jwt.access-token.expiration=3600000",
        "jwt.refresh-token.expiration=86400000"
})

class UserServiceApplicationTests {
    @MockBean
    private MailService mailService;
    @MockBean
    private MailUtil mailUtil;
	@Test
	void contextLoads() {
	}

}
