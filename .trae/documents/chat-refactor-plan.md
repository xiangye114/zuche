
# 智能客服模块重构计划

## 一、项目现状分析

### 1.1 当前架构
- **会话存储**: 使用 MySQL 数据库 (`chat_sessions`, `chat_messages` 表)
- **AI响应**: 简单的关键词匹配，未集成真正的大模型
- **响应方式**: 同步返回，不支持流式输出
- **RAG能力**: 未实现

### 1.2 存在问题
- 会话历史存储在关系型数据库中，读取性能差
- AI响应能力有限，仅支持简单关键词匹配
- 不支持流式返回（打字机效果）
- 未实现RAG增强

## 二、重构目标

### 2.1 技术架构目标
- 使用 **Redis** 存储会话历史上下文
- 集成 **DeepSeek API** 调用大语言模型
- 实现 **SSE (Server-Sent Events)** 流式返回（打字机效果）
- 支持 **RAG增强**（检索租车知识库）

### 2.2 核心流程
```
用户发消息 → HTTP POST
    ↓
会话管理：从 Redis 取历史上下文
    ↓
RAG检索（可选）：检索租车知识库
    ↓
构建提示词：系统提示 + 知识库 + 历史 + 用户问题
    ↓
调用 DeepSeek API
    ↓
流式返回（SSE打字机效果）
    ↓
保存新对话到 Redis
```

## 三、重构步骤

### 3.1 步骤概览

| 序号 | 步骤 | 描述 | 状态 |
|:---:|------|------|:---:|
| 1 | 清理旧代码和数据 | 删除旧的数据库表和相关代码 | Pending |
| 2 | 更新 pom.xml | 添加必要依赖 | Pending |
| 3 | 更新配置文件 | 更新 application.yml 配置 | Pending |
| 4 | 创建配置类 | 创建 ChatConfig 配置类 | Pending |
| 5 | 创建 DTO | 创建 Redis 消息存储 DTO | Pending |
| 6 | 创建服务层 | 创建 RedisChatService、RAGService、AiService | Pending |
| 7 | 修改业务层 | 修改 ChatService 和 ChatServiceImpl | Pending |
| 8 | 修改控制器 | 修改 ChatController，添加流式接口 | Pending |
| 9 | 更新前端 | 更新前端 API 和 Chat 组件 | Pending |

### 3.2 详细步骤

#### 步骤1：清理旧代码和数据

**操作**:
- 删除数据库中 `chat_sessions` 和 `chat_messages` 表
- 保留 `knowledge_base` 表用于 RAG

**SQL语句**:
```sql
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS chat_sessions;
```

#### 步骤2：更新 pom.xml

添加必要依赖：

```xml
<!-- WebFlux for SSE -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- HTTP Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### 步骤3：更新配置文件

更新 `application.yml`，添加 DeepSeek 配置：

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: zuche-server
  
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    serialization:
      write-dates-as-timestamps: false
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/zuche?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 1234
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 100MB
  
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
      lettuce:
        pool:
          max-active: 8
          max-wait: -1
          max-idle: 8
          min-idle: 0

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.zuche.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: zuche-online-car-rental-website-jwt-secret-key-2024
  expiration: 86400000

# DeepSeek AI 配置
deepseek:
  api:
    key: sk-d123ae5c3dfa483eb79945688dde7e58
    url: https://api.deepseek.com/chat/completions
    model: deepseek-chat

# 会话配置
chat:
  session:
    timeout: 86400  # 会话超时时间（秒）
    max-history: 50  # 最大历史消息数

upload:
  path: ./uploads
  url-prefix: /uploads

logging:
  level:
    com.zuche: debug
    org.springframework.security: info
```

#### 步骤4：创建配置类

创建 `ChatConfig.java`：

```java
package com.zuche.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ChatConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
```

#### 步骤5：创建 DTO

创建 `ChatMessageDTO.java`：

```java
package com.zuche.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String role;      // user/assistant/system
    private String content;   // 消息内容
    private Long timestamp;   // 时间戳
}
```

创建 `ChatSessionDTO.java`：

```java
package com.zuche.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    private String sessionId;
    private Long userId;
    private String title;
    @Builder.Default
    private List<ChatMessageDTO> messages = new ArrayList<>();
    private Long createTime;
    private Long updateTime;
}
```

创建 `ChatRequestDTO.java`：

```java
package com.zuche.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestDTO {
    private String sessionId;
    private String content;
    private Boolean useRag = false;
}
```

#### 步骤6：创建服务层

**RedisChatService.java** - 会话管理服务：

```java
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
```

**RAGService.java** - RAG检索服务：

```java
package com.zuche.service;

import com.zuche.entity.KnowledgeBase;

import java.util.List;

public interface RAGService {
    List<KnowledgeBase> retrieve(String query, int topK);
    String buildRagContext(String query);
}
```

**AiService.java** - AI调用服务：

```java
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
```

#### 步骤7：创建服务实现类

**RedisChatServiceImpl.java**：

```java
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
```

**RAGServiceImpl.java**：

```java
package com.zuche.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zuche.entity.KnowledgeBase;
import com.zuche.mapper.KnowledgeBaseMapper;
import com.zuche.service.RAGService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RAGServiceImpl implements RAGService {

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public List<KnowledgeBase> retrieve(String query, int topK) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getStatus, 1)
               .orderByDesc(KnowledgeBase::getPriority)
               .last("LIMIT " + topK);
        
        List<KnowledgeBase> allKnowledge = knowledgeBaseMapper.selectList(wrapper);
        
        return allKnowledge.stream()
                .filter(kb -> isRelevant(kb, query))
                .limit(topK)
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isRelevant(KnowledgeBase kb, String query) {
        if (kb.getQuestion() == null || query == null) {
            return false;
        }
        String lowerQuery = query.toLowerCase();
        String lowerQuestion = kb.getQuestion().toLowerCase();
        return lowerQuery.contains(lowerQuestion) || lowerQuestion.contains(lowerQuery);
    }

    @Override
    public String buildRagContext(String query) {
        List<KnowledgeBase> relevantKnowledge = retrieve(query, 3);
        
        if (relevantKnowledge.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("参考信息：\n");
        for (int i = 0; i < relevantKnowledge.size(); i++) {
            KnowledgeBase kb = relevantKnowledge.get(i);
            context.append((i + 1)).append(". ")
                   .append("问题：").append(kb.getQuestion()).append("\n")
                   .append("回答：").append(kb.getAnswer()).append("\n\n");
        }
        
        return context.toString();
    }
}
```

**AiServiceImpl.java**：

```java
package com.zuche.service.impl;

import com.zuche.dto.ChatMessageDTO;
import com.zuche.service.AiService;
import com.zuche.service.RAGService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    @Resource
    private WebClient webClient;

    @Resource
    private RAGService ragService;

    @Resource
    private ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = "你是一个专业的租车服务智能客服助手，名字是小甜甜。请使用中文回答用户问题，保持友好和专业。";

    @Override
    public String getResponse(List<ChatMessageDTO> history, String userMessage) {
        List<Map<String, String>> messages = buildMessages(history, userMessage, false);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我现在有点忙，请稍后再试。";
        }
    }

    @Override
    public Flux<String> streamResponse(List<ChatMessageDTO> history, String userMessage) {
        return streamResponseInternal(history, userMessage, false);
    }

    @Override
    public String getResponseWithRag(List<ChatMessageDTO> history, String userMessage) {
        List<Map<String, String>> messages = buildMessages(history, userMessage, true);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "抱歉，我现在有点忙，请稍后再试。";
        }
    }

    @Override
    public Flux<String> streamResponseWithRag(List<ChatMessageDTO> history, String userMessage) {
        return streamResponseInternal(history, userMessage, true);
    }

    private Flux<String> streamResponseInternal(List<ChatMessageDTO> history, String userMessage, boolean useRag) {
        List<Map<String, String>> messages = buildMessages(history, userMessage, useRag);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line != null && !line.isEmpty() && !line.equals("data: [DONE]"))
                .map(line -> {
                    if (line.startsWith("data: ")) {
                        line = line.substring(6);
                    }
                    try {
                        JsonNode jsonNode = objectMapper.readTree(line);
                        JsonNode choices = jsonNode.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                return delta.get("content").asText();
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors for non-JSON lines
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }

    private List<Map<String, String>> buildMessages(List<ChatMessageDTO> history, String userMessage, boolean useRag) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        StringBuilder systemContent = new StringBuilder(SYSTEM_PROMPT);
        if (useRag) {
            String ragContext = ragService.buildRagContext(userMessage);
            if (!ragContext.isEmpty()) {
                systemContent.append("\n\n").append(ragContext);
            }
        }
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemContent.toString());
        messages.add(systemMessage);

        for (ChatMessageDTO msg : history) {
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        Map<String, String> userMessageMap = new HashMap<>();
        userMessageMap.put("role", "user");
        userMessageMap.put("content", userMessage);
        messages.add(userMessageMap);

        return messages;
    }
}
```

#### 步骤8：修改业务层

修改 `ChatService.java`：

```java
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
```

修改 `ChatServiceImpl.java`：

```java
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
        ChatSessionDTO session = getSession(userId, sessionId);
        
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
            String fullResponse = session.getMessages().stream()
                    .filter(m -> "assistant".equals(m.getRole()))
                    .reduce((first, second) -> second)
                    .map(ChatMessageDTO::getContent)
                    .orElse("");
            
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
```

#### 步骤9：修改控制器

修改 `ChatController.java`：

```java
package com.zuche.controller;

import com.zuche.common.Result;
import com.zuche.dto.ChatMessageDTO;
import com.zuche.dto.ChatRequestDTO;
import com.zuche.dto.ChatSessionDTO;
import com.zuche.entity.KnowledgeBase;
import com.zuche.service.ChatService;
import com.zuche.utils.JwtUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private JwtUtils jwtUtils;

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtils.getUserId(token);
    }

    @PostMapping("/sessions")
    public Result<ChatSessionDTO> createSession(HttpServletRequest request) {
        Long userId = getUserId(request);
        ChatSessionDTO session = chatService.createSession(userId);
        return Result.success(session);
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionDTO>> getSessions(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<ChatSessionDTO> sessions = chatService.getSessions(userId);
        return Result.success(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<ChatSessionDTO> getSession(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = getUserId(request);
        ChatSessionDTO session = chatService.getSession(userId, sessionId);
        return Result.success(session);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<String> deleteSession(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = getUserId(request);
        chatService.deleteSession(userId, sessionId);
        return Result.success("删除成功");
    }

    @PostMapping("/send")
    public Result<ChatMessageDTO> sendMessage(@RequestBody ChatRequestDTO requestDTO, HttpServletRequest request) {
        Long userId = getUserId(request);
        ChatMessageDTO response = chatService.sendMessage(
                userId, 
                requestDTO.getSessionId(), 
                requestDTO.getContent(),
                Boolean.TRUE.equals(requestDTO.getUseRag())
        );
        return Result.success(response);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamMessage(
            @RequestParam String sessionId,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean useRag,
            HttpServletRequest request) {
        
        Long userId = getUserId(request);
        
        return chatService.streamMessage(userId, sessionId, content, useRag)
                .map(contentPart -> "data: " + contentPart + "\n\n");
    }

    @GetMapping("/faq")
    public Result<List<KnowledgeBase>> getFaq() {
        List<KnowledgeBase> faq = chatService.getFaq();
        return Result.success(faq);
    }
}
```

#### 步骤10：更新前端

更新 `chat.js` API：

```javascript
import request from '@/utils/request'

export const chatApi = {
  createSession: () => request.post('/chat/sessions'),
  getSessions: () => request.get('/chat/sessions'),
  getSession: (sessionId) => request.get(`/chat/sessions/${sessionId}`),
  send: (data) => request.post('/chat/send', data),
  deleteSession: (sessionId) => request.delete(`/chat/sessions/${sessionId}`),
  getFaq: () => request.get('/chat/faq'),
  stream: (sessionId, content, useRag = false) => {
    return new EventSource(`/chat/stream?sessionId=${sessionId}&content=${encodeURIComponent(content)}&useRag=${useRag}`)
  }
}
```

## 四、关键设计

### 4.1 Redis Key 设计

| Key 格式 | 说明 | 示例 |
|----------|------|------|
| `chat:session:{userId}:{sessionId}` | 会话详情 | `chat:session:1:abc123` |
| `chat:history:{userId}:{sessionId}` | 消息历史列表 | `chat:history:1:abc123` |
| `chat:sessions:{userId}` | 用户会话ID列表 | `chat:sessions:1` |

### 4.2 提示词构建

```
系统提示词 + RAG检索结果 + 历史消息 + 当前问题
```

系统提示词：
```
你是一个专业的租车服务智能客服助手，名字是小甜甜。请使用中文回答用户问题，保持友好和专业。
```

### 4.3 流式响应格式

使用 SSE (Server-Sent Events)，响应格式：

```
data: {"type": "chunk", "content": "你"}
data: {"type": "chunk", "content": "好"}
data: {"type": "chunk", "content": "！"}
data: {"type": "done"}
```

## 五、风险与注意事项

### 5.1 潜在风险

| 风险 | 描述 | 缓解措施 |
|------|------|----------|
| API调用成本 | 大模型调用存在费用 | 设置调用限额和监控告警 |
| Redis性能 | 大量会话可能影响性能 | 合理设置过期时间和分片 |
| 数据一致性 | Redis缓存与数据库同步 | 使用合适的缓存策略 |
| 网络延迟 | 流式响应可能受网络影响 | 添加重连机制 |

### 5.2 注意事项

1. 需要确保 DeepSeek API Key 有效
2. 确保 Redis 服务正常运行
3. 考虑消息历史的大小限制
4. 需要处理 API 调用失败的降级策略

## 六、测试计划

### 6.1 单元测试

- 测试 RedisChatService 的会话管理功能
- 测试 RAGService 的检索能力
- 测试 AiService 的调用和流式返回

### 6.2 集成测试

- 测试完整的消息发送流程
- 测试流式返回效果
- 测试会话超时和清理机制

## 七、部署与上线

1. 确保 Redis 服务已部署
2. 配置 DeepSeek API Key
3. 启动应用进行测试
4. 灰度发布，监控运行状态

---

**计划版本**: v1.1  
**创建时间**: 2026-04-18  
**适用项目**: 在线租车网站智能客服模块
