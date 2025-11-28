package tr.com.analizer.qdrant;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QdrantService {
    private final RestTemplate restTemplate = new RestTemplate();

    public void storeInQdrant(int id, List<Double> vector, String text, String fileName) {
        ensureCollectionExists();

        Map<String, Object> point = Map.of(
                "id", id,
                "vector", vector,
                "payload", Map.of(
                        "text", text,
                        "source", fileName
                )
        );

        Map<String, Object> body = Map.of("points", List.of(point));
        restTemplate.put("http://localhost:6333/collections/docs/points", body);
    }


    private void ensureCollectionExists() {
        Map<String, Object> vectors = Map.of("size", 768, "distance", "Cosine");

        try {
            restTemplate.put("http://localhost:6333/collections/docs", Map.of("vectors", vectors));
        } catch (Exception e) {
            if (!e.getMessage().contains("exists")) throw e;
        }
    }

    public List<String> searchQdrant(List<Double> vector) {
        Map<String, Object> body = Map.of(
                "vector", vector,
                "top", 5,
                "with_payload", true
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity("http://localhost:6333/collections/docs/points/search", body, Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("result");

        return results.stream()
                .map(r -> ((Map<String, Object>) r.get("payload")).get("text").toString())
                .collect(Collectors.toList());
    }
}
