package tr.com.w124ai.ollama;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private final RestTemplate restTemplate;
    private final String ollamaBaseUrl = "http://localhost:11434";

    public OllamaService() {
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
