package com.feusap.writing.inference;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/** Response of the Python inference service (POST /analyze). */
public record AnalysisResult(
        double score,
        String band,
        Map<String, Object> features,
        Map<String, Double> contributions,
        List<String> feedback,
        @JsonProperty("model_version") String modelVersion
) {
}
