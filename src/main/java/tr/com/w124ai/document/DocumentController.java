package tr.com.w124ai.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        documentService.processAndStoreDocument(file);
        return ResponseEntity.ok("Document uploaded and embedded successfully.");
    }

    @PostMapping("/query")
    public ResponseEntity<String> ask(@RequestParam String question) throws Exception {
        return ResponseEntity.ok(documentService.query(question));
    }
}
