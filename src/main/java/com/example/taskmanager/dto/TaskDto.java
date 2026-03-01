package com.example.taskmanager.dto;

import com.example.taskmanager.entity.TaskStatus;

public record TaskDto(
        Long id,
        String title,
        String description,
        TaskStatus status
) {}
