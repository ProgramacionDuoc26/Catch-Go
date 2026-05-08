package com.catchandgo.jobs.dto;

import java.time.LocalDateTime;

public record JobApplicationDto(
    Long id,
    Long jobId,
    String jobTitle,
    String userId,
    String estado,
    LocalDateTime fechaPostulacion
) {}
