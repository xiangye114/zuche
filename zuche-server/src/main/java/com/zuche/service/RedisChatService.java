
package com.zuche.service;

import com.zuche.dto.ChatMessageDTO;
import com.zuche.dto.ChatSessionDTO;

import java.util.List;

public interface RedisChatService {
    ChatSessionDTO createSession(Long userId);
    ChatSessionDTO getSession(Long userId, String sessionId);
    List<ChatSessionDTO> getSessions(Long userId);
    void deleteSession(Long userId, String sessionId);
    void addMessage(String sessionKey, ChatMessageDTO message);
    List<ChatMessageDTO> getHistory(String sessionKey);
    void saveSession(ChatSessionDTO session);
}
