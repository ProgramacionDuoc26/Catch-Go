package com.catchandgo.jobs.messaging.admin.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record RetentionRequest(@NotNull @Positive Long maxLen) {}
