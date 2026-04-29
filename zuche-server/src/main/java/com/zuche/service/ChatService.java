
package com.zuche.service;

import com.zuche.dto.ChatMessageDTO;
import com.zuche.dto.ChatSessionDTO;
import com.zuche.entity.KnowledgeBase;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {
    ChatSessionDTO createSession(Long userId);
    List<ChatSessionDTO> getSessions(Long userId);
    ChatSessionDTO getSession(Long userId, String sessionId);
    void deleteSession(Long userId, String sessionId);
    ChatMessageDTO sendMessage(Long userId, String sessionId, String content, boolean useRag);
    Flux<String> streamMessage(Long userId, String sessionId, String content, boolean useRag);
    List<KnowledgeBase> getFaq();
}
