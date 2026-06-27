package com.slowfit.slowfit.domain.comment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentResponseDto {

    private Long id;
    private String username;
    private String mentionUsername;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
