package com.slowfit.slowfit.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private MessageType type;
    private Long roomId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    public enum MessageType {
        ENTER,
        TALK,
        LEAVE
    }
}
