package com.kamil.recruitment;


import dev.langchain4j.data.segment.*;
import dev.langchain4j.memory.*;
import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.inmemory.*;
import org.springframework.context.annotation.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Configuration
public class AppConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    ChatMemory chatMemory(Tokenizer tokenizer) {
        return TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
    }
}