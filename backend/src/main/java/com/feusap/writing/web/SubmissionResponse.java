package com.feusap.writing.web;

import com.feusap.writing.submission.Submission;
import com.feusap.writing.submission.TaskType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        TaskType task,
        double score,
        String band,
        List<String> feedback,
        Map<String, Object> features,
        String modelVersion,
        Instant createdAt
) {
    static SubmissionResponse from(Submission s) {
        return new SubmissionResponse(s.getId(), s.getTask(), s.getScore(), s.getBand(), s.getFeedback(),
                s.getFeatures(), s.getModelVersion(), s.getCreatedAt());
    }
}
