package com.feusap.writing;

import com.feusap.writing.inference.AnalysisResult;
import com.feusap.writing.inference.InferenceClient;
import com.feusap.writing.inference.InferenceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full stack against a real PostgreSQL (Testcontainers, Flyway migrations applied).
 * Only the remote AI model is replaced by a mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SubmissionApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private InferenceClient inferenceClient;

    @Test
    void submitThenReadBack() throws Exception {
        when(inferenceClient.analyze(anyString())).thenReturn(new AnalysisResult(
                12.4, "B2", Map.of("word_count", 130), Map.of("length", 1.15),
                List.of("Bonne structure générale."), "baseline-0.1.0"));

        String location = mvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"task": "TACHE_3", "text": "Je pense que le télétravail est une bonne chose."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/submissions/")))
                .andExpect(jsonPath("$.score").value(12.4))
                .andExpect(jsonPath("$.band").value("B2"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task").value("TACHE_3"))
                .andExpect(jsonPath("$.feedback[0]").value("Bonne structure générale."))
                .andExpect(jsonPath("$.features.word_count").value(130));

        mvc.perform(get("/api/v1/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].band").value("B2"));
    }

    @Test
    void blankTextIsRejected() throws Exception {
        mvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\": \"TACHE_1\", \"text\": \"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void modelDownReturns503() throws Exception {
        when(inferenceClient.analyze(anyString())).thenThrow(new InferenceUnavailableException("down", null));

        mvc.perform(post("/api/v1/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"task\": \"TACHE_2\", \"text\": \"Bonjour à tous.\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Inference service unavailable"));
    }

    @Test
    void unknownSubmissionReturns404() throws Exception {
        mvc.perform(get("/api/v1/submissions/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
