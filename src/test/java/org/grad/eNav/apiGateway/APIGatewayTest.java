package org.grad.eNav.apiGateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource("classpath:application.properties")
class APIGatewayTest {

    @Test
    void contextLoads() {

    }

}