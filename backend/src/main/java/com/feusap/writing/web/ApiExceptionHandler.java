package com.feusap.writing.web;

import com.feusap.writing.inference.InferenceUnavailableException;
import com.feusap.writing.submission.SubmissionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SubmissionNotFoundException.class)
    ProblemDetail notFound(SubmissionNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InferenceUnavailableException.class)
    ProblemDetail inferenceDown(InferenceUnavailableException e) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The scoring service is temporarily unavailable. Try again in a moment.");
        problem.setTitle("Inference service unavailable");
        return problem;
    }
}
