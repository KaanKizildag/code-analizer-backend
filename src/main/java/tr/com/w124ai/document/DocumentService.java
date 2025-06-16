package tr.com.w124ai.document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void processAndStoreDocument(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(bytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            System.out.println("Extracted text length: " + text.length());
            System.out.println("Extracted text snippet: " + text.substring(0, Math.min(500, text.length())));

            List<String> chunks = splitTextToChunks(text, 300);

            for (int i = 0; i < chunks.size(); i++) {
                List<Double> embedding = getEmbedding(chunks.get(i));
                storeInQdrant(i, embedding, chunks.get(i));
            }
        }
    }

    private List<String> splitTextToChunks(String text, int chunkSize) {
        // Basit olarak metni chunkSize büyüklüğünde bölüyoruz
        List<String> chunks = new java.util.ArrayList<>();
        int length = text.length();
        for (int start = 0; start < length; start += chunkSize) {
            int end = Math.min(length, start + chunkSize);
            chunks.add(text.substring(start, end));
        }
        return chunks;
    }


    public String query(String question) throws Exception {
        List<Double> questionVec = getEmbedding(question);
        List<String> matchedChunks = searchQdrant(questionVec);

        String context = String.join("\n", matchedChunks);
        return queryOllama(context, question);
    }

    private List<Double> getEmbedding(String text) {
        Map<String, Object> body = Map.of("model", "nomic-embed-text", "prompt", text);
        ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity("http://localhost:11434/api/embeddings", body, Map.class);
        return (List<Double>) mapResponseEntity.getBody().get("embedding");
    }

    private void ensureCollectionExists() {
        Map<String, Object> vectors = Map.of(
                "size", 768,
                "distance", "Cosine"
        );

        try {
            restTemplate.put("http://localhost:6333/collections/docs", Map.of("vectors", vectors));
        } catch (Exception e) {
            if (!e.getMessage().contains("already exists")) {
                throw e;
            }
        }
    }

    private void storeInQdrant(int id, List<Double> vector, String text) {
        ensureCollectionExists();

        Map<String, Object> point = Map.of(
                "id", id,
                "vector", vector,
                "payload", Map.of("text", text)
        );

        Map<String, Object> body = Map.of(
                "points", List.of(point)
        );

        restTemplate.put("http://localhost:6333/collections/docs/points", body);
    }



    private List<String> searchQdrant(List<Double> vector) {
        Map<String, Object> body = Map.of(
                "vector", vector,
                "top", 3,
                "with_payload", true
        );
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:6333/collections/docs/points/search", body, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("result");
        return results.stream()
                .map(r -> ((Map<String, Object>) r.get("payload")).get("text").toString())
                .collect(Collectors.toList());
    }


    private String queryOllama(String context, String question) {
        String prompt = "Context:\n" + context + "\n\nQuestion: " + question;
        Map<String, Object> body = Map.of("model", "gemma3:4b", "prompt", prompt, "stream", false);
        ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:11434/api/generate", body, Map.class);
        return response.getBody().get("response").toString();
    }
}
