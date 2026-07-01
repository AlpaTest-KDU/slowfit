package com.slowfit.slowfit.domain.post.dto;

import com.slowfit.slowfit.domain.post.entity.BoardType;
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
public class PostResponseDto {

    private Long id;
    private String username;
    private BoardType boardType;
    private String title;
    private String content;
    private String pace;
    private String courseUrl;
    private String imageUrl;
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
