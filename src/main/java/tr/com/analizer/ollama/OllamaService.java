package tr.com.analizer.ollama;

import org.slf4j.Logger;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(OllamaService.class);
    private final RestTemplate restTemplate;
    private final String ollamaBaseUrl = "http://localhost:11434";
    private final ChatModel chatModel;

    public OllamaService() {
        this.chatModel = OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(ollamaBaseUrl))
                .defaultOptions(
                        OllamaOptions.builder()
                                .temperature(0.1)
                                .model("deepseek-coder:1.3b")
                                .build()
                )
                .build();
        this.restTemplate = new RestTemplate();
    }

    public String queryOllama(String context, String question) {
        String prompt = "You are a strict code-generation assistant. Context:\n" + context + "\n\nQuestion: " + question;

        Map<String, Object> body = Map.of(
                "model", "deepseek-coder:1.3b",
                "prompt", prompt,
                "stream", false
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(ollamaBaseUrl + "/api/generate", body, Map.class);

        return response.getBody().get("response").toString();
    }

    public List<Double> getEmbedding(String text) {
        Map<String, Object> body = Map.of("model", "nomic-embed-text", "prompt", text);
        ResponseEntity<Map> resp = restTemplate.postForEntity(ollamaBaseUrl + "/api/embeddings", body, Map.class);
        return (List<Double>) resp.getBody().get("embedding");
    }
}
