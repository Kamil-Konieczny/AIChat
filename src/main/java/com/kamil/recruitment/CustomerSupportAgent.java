package com.kamil.recruitment;

import dev.langchain4j.service.spring.*;

@AiService
interface CustomerSupportAgent {

    String answer(String userMessage);
}