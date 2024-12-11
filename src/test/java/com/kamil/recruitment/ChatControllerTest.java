package com.kamil.recruitment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        Mockito.reset(chatService);
    }

    @Test
    void testUploadDocument_success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Sample document content".getBytes());

        when(chatService.saveDocument(any())).thenReturn("12345");

        mockMvc.perform(multipart("/api/upload")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Document uploaded successfully with ID: 12345"));
    }

    @Test
    void testChatWithDocument_success() throws Exception {
        when(chatService.queryDocument(anyString(), anyString())).thenReturn("This is an answer to the question.");

        mockMvc.perform(post("/api/chat")
                        .param("documentId", "12345")
                        .param("question", "What is the content?")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("This is an answer to the question."));
    }

    @Test
    void testChatWithDocument_failure() throws Exception {
        when(chatService.queryDocument(anyString(), anyString())).thenThrow(new Exception("Document with ID 12345 not found."));

        mockMvc.perform(post("/api/chat")
                        .param("documentId", "12345")
                        .param("question", "What is the content?")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error processing query: Document with ID 12345 not found."));
    }
}
