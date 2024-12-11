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
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
public class ChatServiceImpl implements ChatService{

    private final AllMiniLmL6V2EmbeddingModel embeddingModel;
    private CustomerSupportAgent agent;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final Map<String, Document> documentStore;

    @Autowired
    public ChatServiceImpl(EmbeddingStore<TextSegment> embeddingStore,
                           CustomerSupportAgent agent,
                           AllMiniLmL6V2EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.agent = agent;
        this.embeddingModel = embeddingModel;
        this.documentStore = new ConcurrentHashMap<>();
    }

    public String saveDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
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
                .filter(metadataKey("documentId").isEqualTo(documentId))
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(1)
                .minScore(0.6)
                .build();

        var retrievedContent = contentRetriever.retrieve(Query.from(question));
        System.out.println(retrievedContent);
        if (!retrievedContent.isEmpty()) {
            return agent.answer("Based on the following context: " + retrievedContent + "\nQuestion: " + question);
        }

        return "No relevant content found for your query.";
    }
}

