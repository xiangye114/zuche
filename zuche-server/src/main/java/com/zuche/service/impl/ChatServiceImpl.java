
package com.zuche.service.impl;

import com.zuche.dto.ChatMessageDTO;
import com.zuche.dto.ChatSessionDTO;
import com.zuche.entity.KnowledgeBase;
import com.zuche.exception.BusinessException;
import com.zuche.mapper.KnowledgeBaseMapper;
import com.zuche.service.AiService;
import com.zuche.service.ChatService;
import com.zuche.service.RedisChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private RedisChatService redisChatService;

    @Resource
    private AiService aiService;

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public ChatSessionDTO createSession(Long userId) {
        return redisChatService.createSession(userId);
    }

    @Override
    public List<ChatSessionDTO> getSessions(Long userId) {
        return redisChatService.getSessions(userId);
    }

    @Override
    public ChatSessionDTO getSession(Long userId, String sessionId) {
        ChatSessionDTO session = redisChatService.getSession(userId, sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        return session;
    }

    @Override
    public void deleteSession(Long userId, String sessionId) {
        ChatSessionDTO session = redisChatService.getSession(userId, sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        redisChatService.deleteSession(userId, sessionId);
    }

    @Override
    public ChatMessageDTO sendMessage(Long userId, String sessionId, String content, boolean useRag) {
        ChatSessionDTO session = redisChatService.getSession(userId, sessionId);
        
        if (session == null) {
            session = redisChatService.createSession(userId);
        }
        
        ChatMessageDTO userMessage = ChatMessageDTO.builder()
                .role("user")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        
        List<ChatMessageDTO> history = session.getMessages();
        history.add(userMessage);
        
        String aiResponse = useRag 
                ? aiService.getResponseWithRag(history, content)
                : aiService.getResponse(history, content);
        
        ChatMessageDTO assistantMessage = ChatMessageDTO.builder()
                .role("assistant")
                .content(aiResponse)
                .timestamp(System.currentTimeMillis())
                .build();
        
        history.add(assistantMessage);
        session.setMessages(history);
        
        if ("新对话".equals(session.getTitle())) {
            session.setTitle(content.length() > 20 ? content.substring(0, 20) + "..." : content);
        }
        
        redisChatService.saveSession(session);
        
        return assistantMessage;
    }

    @Override
    public Flux<String> streamMessage(Long userId, String sessionId, String content, boolean useRag) {
        ChatSessionDTO session = getSession(userId, sessionId);
        
        ChatMessageDTO userMessage = ChatMessageDTO.builder()
                .role("user")
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
        
        session.getMessages().add(userMessage);
        redisChatService.saveSession(session);
        
        Flux<String> responseFlux = useRag
                ? aiService.streamResponseWithRag(session.getMessages(), content)
                : aiService.streamResponse(session.getMessages(), content);
        
        return responseFlux.doOnComplete(() -> {
            StringBuilder fullResponseBuilder = new StringBuilder();
            for (ChatMessageDTO msg : session.getMessages()) {
                if ("assistant".equals(msg.getRole())) {
                    fullResponseBuilder.append(msg.getContent());
                }
            }
            String fullResponse = fullResponseBuilder.toString();
            
            if (!fullResponse.isEmpty()) {
                ChatMessageDTO assistantMessage = ChatMessageDTO.builder()
                        .role("assistant")
                        .content(fullResponse)
                        .timestamp(System.currentTimeMillis())
                        .build();
                
                session.getMessages().add(assistantMessage);
                
                if ("新对话".equals(session.getTitle())) {
                    session.setTitle(content.length() > 20 ? content.substring(0, 20) + "..." : content);
                }
                
                redisChatService.saveSession(session);
            }
        });
    }

    @Override
    public List<KnowledgeBase> getFaq() {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getStatus, 1)
               .orderByDesc(KnowledgeBase::getPriority);
        return knowledgeBaseMapper.selectList(wrapper);
    }
}
