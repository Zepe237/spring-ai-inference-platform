package com.feusap.writing;

import com.feusap.writing.inference.InferenceClient;
import com.feusap.writing.inference.InferenceUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(InferenceClient.class)
@TestPropertySource(properties = "inference.base-url=http://inference.test")
class InferenceClientTest {

    @Autowired
    private InferenceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void mapsTheInferenceResponse() {
        server.expect(requestTo("http://inference.test/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.text").value("Bonjour"))
                .andRespond(withSuccess("""
                        {"score": 9.6, "band": "B1",
                         "features": {"word_count": 70},
                         "contributions": {"length": 0.62},
                         "feedback": ["Texte trop court"],
                         "model_version": "baseline-0.1.0"}
                        """, MediaType.APPLICATION_JSON));

        var result = client.analyze("Bonjour");

        assertThat(result.score()).isEqualTo(9.6);
        assertThat(result.band()).isEqualTo("B1");
        assertThat(result.feedback()).containsExactly("Texte trop court");
        assertThat(result.modelVersion()).isEqualTo("baseline-0.1.0");
        server.verify();
    }

    @Test
    void serverErrorBecomesInferenceUnavailable() {
        server.expect(requestTo("http://inference.test/analyze")).andRespond(withServerError());

        assertThatThrownBy(() -> client.analyze("Bonjour"))
                .isInstanceOf(InferenceUnavailableException.class);
    }
}
