package com.feusap.writing.submission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    Page<Submission> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
