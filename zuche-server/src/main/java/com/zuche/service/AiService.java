
package com.zuche.service;

import com.zuche.dto.ChatMessageDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiService {
    String getResponse(List<ChatMessageDTO> history, String userMessage);
    Flux<String> streamResponse(List<ChatMessageDTO> history, String userMessage);
    String getResponseWithRag(List<ChatMessageDTO> history, String userMessage);
    Flux<String> streamResponseWithRag(List<ChatMessageDTO> history, String userMessage);
}
