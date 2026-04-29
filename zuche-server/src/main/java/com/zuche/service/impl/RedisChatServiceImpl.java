
package com.zuche.service.impl;

import com.zuche.dto.ChatMessageDTO;
import com.zuche.dto.ChatSessionDTO;
import com.zuche.service.RedisChatService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisChatServiceImpl implements RedisChatService {

    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String HISTORY_KEY_PREFIX = "chat:history:";
    private static final String SESSIONS_KEY_PREFIX = "chat:sessions:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ChatSessionDTO createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        
        ChatSessionDTO session = ChatSessionDTO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title("新对话")
                .messages(new ArrayList<>())
                .createTime(now)
                .updateTime(now)
                .build();

        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + sessionId;
        String sessionsKey = SESSIONS_KEY_PREFIX + userId;

        redisTemplate.opsForValue().set(sessionKey, session, 24, TimeUnit.HOURS);
        redisTemplate.opsForList().leftPush(sessionsKey, sessionId);
        redisTemplate.expire(sessionsKey, 24, TimeUnit.HOURS);

        return session;
    }

    @Override
    public ChatSessionDTO getSession(Long userId, String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + sessionId;
        Object sessionObj = redisTemplate.opsForValue().get(sessionKey);
        
        if (sessionObj != null) {
            redisTemplate.expire(sessionKey, 24, TimeUnit.HOURS);
            return (ChatSessionDTO) sessionObj;
        }
        return null;
    }

    @Override
    public List<ChatSessionDTO> getSessions(Long userId) {
        String sessionsKey = SESSIONS_KEY_PREFIX + userId;
        List<Object> sessionIds = redisTemplate.opsForList().range(sessionsKey, 0, -1);
        
        return sessionIds.stream()
                .map(id -> getSession(userId, (String) id))
                .filter(session -> session != null)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(Long userId, String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + userId + ":" + sessionId;
        String historyKey = HISTORY_KEY_PREFIX + userId + ":" + sessionId;
        String sessionsKey = SESSIONS_KEY_PREFIX + userId;

        redisTemplate.delete(sessionKey);
        redisTemplate.delete(historyKey);
        redisTemplate.opsForList().remove(sessionsKey, 0, sessionId);
    }

    @Override
    public void addMessage(String sessionKey, ChatMessageDTO message) {
        String historyKey = HISTORY_KEY_PREFIX + sessionKey.substring(SESSION_KEY_PREFIX.length());
        redisTemplate.opsForList().rightPush(historyKey, message);
        redisTemplate.expire(historyKey, 24, TimeUnit.HOURS);
    }

    @Override
    public List<ChatMessageDTO> getHistory(String sessionKey) {
        String historyKey = HISTORY_KEY_PREFIX + sessionKey.substring(SESSION_KEY_PREFIX.length());
        List<Object> messages = redisTemplate.opsForList().range(historyKey, 0, -1);
        
        return messages.stream()
                .map(obj -> (ChatMessageDTO) obj)
                .collect(Collectors.toList());
    }

    @Override
    public void saveSession(ChatSessionDTO session) {
        String sessionKey = SESSION_KEY_PREFIX + session.getUserId() + ":" + session.getSessionId();
        session.setUpdateTime(System.currentTimeMillis());
        redisTemplate.opsForValue().set(sessionKey, session, 24, TimeUnit.HOURS);
    }
}
