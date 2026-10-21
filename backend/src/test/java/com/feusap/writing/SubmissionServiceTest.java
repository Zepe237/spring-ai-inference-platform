package com.feusap.writing;

import com.feusap.writing.inference.AnalysisResult;
import com.feusap.writing.inference.InferenceClient;
import com.feusap.writing.inference.InferenceUnavailableException;
import com.feusap.writing.submission.Submission;
import com.feusap.writing.submission.SubmissionNotFoundException;
import com.feusap.writing.submission.SubmissionRepository;
import com.feusap.writing.submission.SubmissionService;
import com.feusap.writing.submission.TaskType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubmissionServiceTest {

    private final SubmissionRepository repository = mock(SubmissionRepository.class);
    private final InferenceClient inferenceClient = mock(InferenceClient.class);
    private final SubmissionService service = new SubmissionService(repository, inferenceClient);

    @Test
    void storesTheScoreReturnedByTheModel() {
        when(inferenceClient.analyze("Mon texte")).thenReturn(new AnalysisResult(
                12.4, "B2", Map.of("word_count", 130), Map.of("length", 1.2),
                List.of("Bonne structure"), "baseline-0.1.0"));
        when(repository.save(any(Submission.class))).thenAnswer(inv -> inv.getArgument(0));

        Submission saved = service.submit(TaskType.TACHE_3, "Mon texte");

        assertThat(saved.getScore()).isEqualTo(12.4);
        assertThat(saved.getBand()).isEqualTo("B2");
        assertThat(saved.getFeedback()).containsExactly("Bonne structure");
        assertThat(saved.getModelVersion()).isEqualTo("baseline-0.1.0");
        assertThat(saved.getTask()).isEqualTo(TaskType.TACHE_3);
    }

    @Test
    void doesNotStoreAnythingWhenTheModelIsDown() {
        when(inferenceClient.analyze(any())).thenThrow(new InferenceUnavailableException("down", null));

        assertThatThrownBy(() -> service.submit(TaskType.TACHE_1, "Texte"))
                .isInstanceOf(InferenceUnavailableException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void unknownIdIsReportedAsNotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(id)).isInstanceOf(SubmissionNotFoundException.class);
    }
}
