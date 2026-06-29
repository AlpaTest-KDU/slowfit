package com.slowfit.slowfit.domain.chat.repository;

import com.slowfit.slowfit.domain.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomId(Long chatRoomId);
}
