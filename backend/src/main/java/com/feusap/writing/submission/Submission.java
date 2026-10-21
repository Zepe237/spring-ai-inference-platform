package com.feusap.writing.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "submission")
public class Submission {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskType task;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false, length = 4)
    private String band;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> feedback = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> features;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Submission() {
        // for JPA
    }

    public Submission(TaskType task, String text, double score, String band, List<String> feedback,
                      Map<String, Object> features, String modelVersion) {
        this.id = UUID.randomUUID();
        this.task = task;
        this.text = text;
        this.score = score;
        this.band = band;
        this.feedback = new ArrayList<>(feedback);
        this.features = features;
        this.modelVersion = modelVersion;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public TaskType getTask() { return task; }
    public String getText() { return text; }
    public double getScore() { return score; }
    public String getBand() { return band; }
    public List<String> getFeedback() { return feedback; }
    public Map<String, Object> getFeatures() { return features; }
    public String getModelVersion() { return modelVersion; }
    public Instant getCreatedAt() { return createdAt; }
}
