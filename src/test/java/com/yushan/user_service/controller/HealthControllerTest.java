package com.yushan.user_service.controller;

import com.yushan.user_service.dao.UserMapper;
import com.yushan.user_service.event.UserActivityEventProducer;
import com.yushan.user_service.service.UserService;
import com.yushan.user_service.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class HealthControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserActivityEventProducer userActivityEventProducer;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void health_shouldReturnServiceStatus() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("user-service"))
                .andExpect(jsonPath("$.message").value("User Service is running!"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
