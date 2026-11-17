package com.catchandgo.jobs.messaging.admin.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record CreateGroupRequest(@NotBlank @Pattern(regexp = "^[a-zA-Z0-9:_-]+$") String name) {}
