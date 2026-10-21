package com.feusap.writing.web;

import com.feusap.writing.submission.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final SubmissionService service;

    public SubmissionController(SubmissionService service) {
        this.service = service;
    }

    @Operation(summary = "Submit a written answer and get it scored by the AI service")
    @ApiResponse(responseCode = "201", description = "Submission scored and stored")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "503", description = "Inference service unavailable")
    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody CreateSubmissionRequest request) {
        var submission = service.submit(request.task(), request.text());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(submission.getId()).toUri();
        return ResponseEntity.created(location).body(SubmissionResponse.from(submission));
    }

    @Operation(summary = "Get one submission")
    @GetMapping("/{id}")
    public SubmissionResponse get(@PathVariable UUID id) {
        return SubmissionResponse.from(service.get(id));
    }

    @Operation(summary = "List submissions, newest first")
    @GetMapping
    public Page<SubmissionResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.list(pageable).map(SubmissionResponse::from);
    }
}
