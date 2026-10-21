package com.feusap.writing.submission;

import com.feusap.writing.inference.AnalysisResult;
import com.feusap.writing.inference.InferenceClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubmissionService {

    private final SubmissionRepository repository;
    private final InferenceClient inferenceClient;

    public SubmissionService(SubmissionRepository repository, InferenceClient inferenceClient) {
        this.repository = repository;
        this.inferenceClient = inferenceClient;
    }

    /**
     * Scores the text with the AI service, then stores the submission with its result.
     * The method is deliberately not transactional: no database connection is held
     * while waiting for the remote model, and repository.save() runs in its own transaction.
     */
    public Submission submit(TaskType task, String text) {
        AnalysisResult result = inferenceClient.analyze(text);
        return repository.save(new Submission(task, text, result.score(), result.band(), result.feedback(),
                result.features(), result.modelVersion()));
    }

    @Transactional(readOnly = true)
    public Submission get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new SubmissionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Submission> list(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
