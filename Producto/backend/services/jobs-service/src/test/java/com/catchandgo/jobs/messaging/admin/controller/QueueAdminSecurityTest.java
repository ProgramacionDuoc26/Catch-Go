package com.catchandgo.jobs.messaging.admin.controller;

import com.catchandgo.common.jwt.JwtService;
import com.catchandgo.jobs.messaging.admin.service.QueueAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QueueAdminController.class, properties = "JWT_SECRET=test-secret-key-with-at-least-32-bytes-for-tests")
class QueueAdminSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean QueueAdminService adminService;
    private final JwtService jwt = new JwtService("test-secret-key-with-at-least-32-bytes-for-tests");

    @Test void anonymousIsUnauthorized() throws Exception {
        mvc.perform(get("/queues")).andExpect(status().isUnauthorized());
    }
    @Test void ordinaryUserCannotAdministerQueues() throws Exception {
        mvc.perform(get("/queues").header("Authorization", "Bearer " + jwt.generateToken("1", "TRABAJADOR", 60)))
                .andExpect(status().isForbidden());
    }
    @Test void oldTokenWithoutRoleCannotAdministerQueues() throws Exception {
        mvc.perform(get("/api/v1/queues").header("Authorization", "Bearer " + jwt.generateToken("1", 60)))
                .andExpect(status().isForbidden());
    }
    @Test void validAdminCanAdministerQueues() throws Exception {
        mvc.perform(get("/queues").header("Authorization", "Bearer " + jwt.generateToken("1", "ADMIN", 60)))
                .andExpect(status().isOk());
    }
    @Test void invalidAdminSignatureIsUnauthorized() throws Exception {
        var other = new JwtService("another-test-secret-key-with-at-least-32-bytes");
        mvc.perform(get("/queues").header("Authorization", "Bearer " + other.generateToken("1", "ADMIN", 60)))
                .andExpect(status().isUnauthorized());
    }
}
