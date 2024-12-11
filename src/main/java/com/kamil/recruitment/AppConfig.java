package com.kamil.recruitment;


import dev.langchain4j.data.segment.*;
import dev.langchain4j.memory.*;
import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.inmemory.*;
import org.springframework.context.annotation.*;


@Configuration
public class AppConfig {
    //   embedding model for generating text embeddings
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    //   in-memory store for text segment embeddings
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    //   chat memory with a 1000-token sliding window
    @Bean
    ChatMemory chatMemory(Tokenizer tokenizer) {
        return TokenWindowChatMemory.withMaxTokens(1000, tokenizer);
    }
}