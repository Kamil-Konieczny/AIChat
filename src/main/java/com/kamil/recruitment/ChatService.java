package com.kamil.recruitment;

import org.springframework.web.multipart.*;

import java.io.*;

public interface ChatService {
    public String saveDocument(MultipartFile file) throws IOException;
    public String queryDocument(String documentId, String question) throws Exception;
}
