package com.slowfit.slowfit.global.config;

import com.slowfit.slowfit.domain.chat.entity.ChatRoom;
import com.slowfit.slowfit.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ChatRoomRepository chatRoomRepository;

    @Bean
    public ApplicationRunner initChatRoom() {
        return args -> {
            if (chatRoomRepository.count() == 0) {
                ChatRoom chatRoom = ChatRoom.builder()
                        .name("인증 채팅방")
                        .build();
                if (chatRoom == null) {
                    throw new IllegalStateException("Failed to create chat room");
                }
                chatRoomRepository.save(chatRoom);
            }
        };
    }
}
