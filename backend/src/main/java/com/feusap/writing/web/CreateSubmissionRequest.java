package com.feusap.writing.web;

import com.feusap.writing.submission.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubmissionRequest(
        @NotNull(message = "task is required (TACHE_1, TACHE_2 or TACHE_3)")
        TaskType task,
        @NotBlank(message = "text cannot be blank")
        @Size(max = 5000, message = "text must be at most 5000 characters")
        String text
) {
}
