package com.feusap.writing.submission;

import java.util.UUID;

public class SubmissionNotFoundException extends RuntimeException {

    public SubmissionNotFoundException(UUID id) {
        super("Submission " + id + " not found");
    }
}
