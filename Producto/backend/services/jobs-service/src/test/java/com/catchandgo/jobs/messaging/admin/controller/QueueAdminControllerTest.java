package com.catchandgo.jobs.messaging.admin.controller;

import com.catchandgo.common.events.QueueSummaryDto;
import com.catchandgo.jobs.messaging.admin.dto.CreateQueueRequest;
import com.catchandgo.jobs.messaging.admin.service.QueueAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {QueueAdminController.class, com.catchandgo.common.exceptions.GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class QueueAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QueueAdminService adminService;

    @Test
    void createQueue_validRequest_returnsCreated() throws Exception {
        CreateQueueRequest request = new CreateQueueRequest("jobs-stream", "jobs-group", 1000L);
        QueueSummaryDto summary = new QueueSummaryDto(
            "stream:jobs-stream", 0L, 1, "ACTIVE", null, null, Instant.now()
        );

        when(adminService.createQueue(any(CreateQueueRequest.class))).thenReturn(summary);

        mockMvc.perform(post("/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.queueName").value("stream:jobs-stream"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createQueue_blankName_returnsBadRequest() throws Exception {
        // Validación de parámetro vacío de la rúbrica
        CreateQueueRequest invalid = new CreateQueueRequest("", "group", 1000L);

        mockMvc.perform(post("/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listQueues_returnsList() throws Exception {
        QueueSummaryDto summary = new QueueSummaryDto(
            "stream:job-applications", 5L, 1, "ACTIVE", "1-0", "5-0", Instant.now()
        );
        when(adminService.listQueues()).thenReturn(List.of(summary));

        mockMvc.perform(get("/queues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].queueName").value("stream:job-applications"))
                .andExpect(jsonPath("$[0].messageCount").value(5));
    }

    @Test
    void deleteQueue_returnsOk() throws Exception {
        when(adminService.deleteQueue("job-applications")).thenReturn(true);

        mockMvc.perform(delete("/queues/job-applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(true));
    }
}
