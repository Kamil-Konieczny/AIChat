package com.kamil.recruitment;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.embedding.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.*;
import dev.langchain4j.model.output.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.store.embedding.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.mock.web.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {

    @Mock
    private EmbeddingStore<TextSegment> embeddingStore;

    @Mock
    private AllMiniLmL6V2EmbeddingModel embeddingModel;

    @Mock
    private CustomerSupportAgent agent;

    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatServiceImpl(embeddingStore, agent, embeddingModel);
    }
    @Test
    void shouldThrowException_WhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.saveDocument(file);
        });

        assertEquals("File cannot be empty", exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenDocumentNotFound() {
        String documentId = "nonexistent";
        String question = "What is the content?";

        Exception exception = assertThrows(Exception.class, () -> {
            chatService.queryDocument(documentId, question);
        });

        assertEquals("Document with ID nonexistent not found.", exception.getMessage());
    }

    @Test
    void shouldThrowException_WhenQuestionIsEmpty() {
        String documentId = "1234";
        String question = " ";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.queryDocument(documentId, question);
        });

        assertEquals("Question cannot be null or empty", exception.getMessage());
    }
}