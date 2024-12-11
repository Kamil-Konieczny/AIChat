package com.kamil.recruitment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService documentChatService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String documentId = documentChatService.saveDocument(file);
            return ResponseEntity.ok("Document uploaded successfully with ID: " + documentId);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error uploading document: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chatWithDocument(@RequestParam("documentId") String documentId,
                                                   @RequestParam("question") String question) {
        try {
            String answer = documentChatService.queryDocument(documentId, question);
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing query: " + e.getMessage());
        }
    }
}
