
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
            @RequestParam(required = false) String token,
            HttpServletRequest request) {

        Long userId = getUserIdFromToken(token, request);

        return chatService.streamMessage(userId, sessionId, content, useRag)
                .map(contentPart -> "data: " + contentPart + "\n\n");
    }
    
    private Long getUserIdFromToken(String tokenParam, HttpServletRequest request) {
        String token = tokenParam;
        if (token == null || token.isEmpty()) {
            String headerToken = request.getHeader("Authorization");
            if (headerToken != null && headerToken.startsWith("Bearer ")) {
                token = headerToken.substring(7);
            }
        }
        return jwtUtils.getUserId(token);
    }

    @GetMapping("/faq")
    public Result<List<KnowledgeBase>> getFaq() {
        List<KnowledgeBase> faq = chatService.getFaq();
        return Result.success(faq);
    }
}
