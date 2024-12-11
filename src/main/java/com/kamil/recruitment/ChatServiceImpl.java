package com.kamil.recruitment;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.rag.query.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
public class ChatServiceImpl implements ChatService{

    private final AllMiniLmL6V2EmbeddingModel embeddingModel; // Model for generating embeddings.
    private CustomerSupportAgent agent; // Handles answering user queries.
    private final EmbeddingStore<TextSegment> embeddingStore; // Stores embeddings for efficient retrieval.

    private final Map<String, Document> documentStore; // Stores uploaded documents in memory.

    @Autowired
    public ChatServiceImpl(EmbeddingStore<TextSegment> embeddingStore,
                           CustomerSupportAgent agent,
                           AllMiniLmL6V2EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.agent = agent;
        this.embeddingModel = embeddingModel;
        this.documentStore = new ConcurrentHashMap<>();
    }

    /**
     * Saves a document by splitting it into segments, generating embeddings, and storing them.
     *
     * @param file the uploaded document file
     * @return documentId the ID of the document to query
     * @throws IOException in case of error reading the file
     */
    public String saveDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String documentId = UUID.randomUUID().toString();
        String content = new String(file.getBytes());
        Document document = new Document(content);
        documentStore.put(documentId, document);

        // Ingest the new document
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer());
        List<TextSegment> segments = documentSplitter.split(document);
        for (TextSegment segment: segments){
            segment.metadata().put("documentId", documentId);
        }
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);

        return documentId;
    }
    /**
     * Queries a specific document and retrieves relevant content to answer the question.
     *
     * @param documentId the ID of the document to query
     * @param question   the user's question
     * @return the answer generated based on retrieved content
     * @throws Exception if the document is not found or question is invalid
     */
    public String queryDocument(String documentId, String question) throws Exception {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }

        Document document = documentStore.get(documentId);
        if (document == null) {
            throw new Exception("Document with ID " + documentId + " not found.");
        }

        // Retrieve content related to the query
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(metadataKey("documentId").isEqualTo(documentId))
                .maxResults(1)
                .minScore(0.5)
                .build();

        var retrievedContent = contentRetriever.retrieve(Query.from(question));
        System.out.println(retrievedContent);
        if (!retrievedContent.isEmpty()) {
            return agent.answer("Based on the following context: " + retrievedContent + "\nQuestion: " + question);
        }

        return "No relevant content found for your query.";
    }

}

