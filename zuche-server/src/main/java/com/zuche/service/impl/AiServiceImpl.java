
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
        String ragContext = ragService.buildRagContext(userMessage);
        
        if (!ragContext.isEmpty()) {
            return extractAnswerFromRag(ragContext);
        } else {
            return getResponse(history, userMessage);
        }
    }
    
    private String extractAnswerFromRag(String ragContext) {
        if (ragContext.contains("回答：")) {
            int startIndex = ragContext.indexOf("回答：");
            int nextLineIndex = ragContext.indexOf("\n", startIndex + 3);
            if (nextLineIndex > startIndex) {
                return ragContext.substring(startIndex + 3, nextLineIndex).trim();
            }
            return ragContext.substring(startIndex + 3).trim();
        }
        return ragContext;
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
