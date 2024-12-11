package com.kamil.recruitment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
/**
 * REST controller for handling document uploads and chat queries.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatService documentChatService;

    /**
     * Endpoint pointing to the frontend application
     *
     * @return a simple string indicating the API is running.
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Uploads a document and saves it
     *
     * @param file the uploaded file to be processed
     * @return a response containing the document ID or an error message
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String documentId = documentChatService.saveDocument(file);
            return ResponseEntity.ok("Document uploaded successfully with ID: " + documentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(400).body("Error uploading document: " + e.getMessage());
        }
    }

    /**
     * Queries a specific document with a given question and get an answer.
     *
     * @param documentId of the document to query
     * @param question the question to be asked about the document
     * @return a response containing the answer or an error message
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chatWithDocument(@RequestParam("documentId") String documentId,
                                                   @RequestParam("question") String question) {
        try {
            String answer = documentChatService.queryDocument(documentId, question); // Queries the document and generates a response
            return ResponseEntity.ok(answer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing query: " + e.getMessage());
        }
    }
}
