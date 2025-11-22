package tr.com.w124ai.document;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tr.com.w124ai.chunker.Chunker;
import tr.com.w124ai.chunker.ChunkerFactory;
import tr.com.w124ai.ignore.IgnoreFileLoader;
import tr.com.w124ai.ignore.IgnoreRules;
import tr.com.w124ai.ollama.OllamaService;
import tr.com.w124ai.qdrant.QdrantService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentService {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DocumentService.class);

    private static final List<String> SUPPORTED_EXTENSIONS = List.of(
            ".java", ".xml", ".yml", ".yaml", ".json", ".properties", ".md", ".txt"
    );
    private IgnoreRules ignoreRules;

    private final OllamaService ollamaService;
    private final QdrantService qdrantService;

    public DocumentService(OllamaService ollamaService, QdrantService qdrantService) {
        this.ollamaService = ollamaService;
        this.qdrantService = qdrantService;
    }

    @PostConstruct
    public void init() throws Exception {
        File ignoreFile = new File(".gitignore");
        this.ignoreRules = IgnoreFileLoader.loadIgnoreFile(ignoreFile);
    }

    public void processAndStoreDocument(MultipartFile file) throws Exception {

        Map<String, String> extractedFiles = extractZip(file.getBytes());

        int idCounter = 1;

        for (Map.Entry<String, String> entry : extractedFiles.entrySet()) {

            String fileName = entry.getKey();
            String content = entry.getValue();

            Chunker chunker = ChunkerFactory.getChunker(fileName);
            List<String> chunks = chunker.chunk(content);
            log.info("Indexlenen dosya: {} | Boyut: {} | Chunker: {}", fileName, content.length(), chunker.getClass().getName());

            for (String ch : chunks) {
                List<Double> emb = ollamaService.getEmbedding(ch);
                qdrantService.storeInQdrant(idCounter++, emb, ch, fileName);
            }
        }
    }

    private Map<String, String> extractZip(byte[] zipBytes) throws IOException {
        Map<String, String> files = new LinkedHashMap<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                // Klasörleri atla
                if (entry.isDirectory())
                    continue;

                // Sadece desteklenen dosyalar
                if (isSupported(name)) {
                    String content = new String(zis.readAllBytes());
                    files.put(name, content);
                }

                zis.closeEntry();
            }
        }

        return files;
    }

    private boolean isSupported(String fileName) {
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    public String query(String question) throws Exception {
        List<Double> questionVec = ollamaService.getEmbedding(question);
        List<String> matchedChunks = qdrantService.searchQdrant(questionVec);
        String context = String.join("\n", matchedChunks);
        return ollamaService.queryOllama(context, question);
    }
}
