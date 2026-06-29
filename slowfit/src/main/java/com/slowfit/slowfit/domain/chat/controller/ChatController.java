package com.slowfit.slowfit.domain.chat.controller;

import com.slowfit.slowfit.domain.chat.dto.ChatMessageDto;
import com.slowfit.slowfit.domain.chat.entity.ChatMessage;
import com.slowfit.slowfit.domain.chat.entity.ChatRoom;
import com.slowfit.slowfit.domain.chat.repository.ChatMessageRepository;
import com.slowfit.slowfit.domain.chat.repository.ChatRoomRepository;
import com.slowfit.slowfit.domain.user.entitiy.User;
import com.slowfit.slowfit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageDto sendMessage(@DestinationVariable @NonNull Long roomId, @NonNull ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        User user = userRepository.findByUsername(messageDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(messageDto.getContent())
                .build();

        if (chatMessage == null) {
            throw new IllegalStateException("Failed to create chat message");
        }

        chatMessageRepository.save(chatMessage);

        return ChatMessageDto.builder()
                .type(messageDto.getType())
                .roomId(roomId)
                .username(messageDto.getUsername())
                .content(messageDto.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
