
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
