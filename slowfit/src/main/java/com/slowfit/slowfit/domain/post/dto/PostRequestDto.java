package com.slowfit.slowfit.domain.post.dto;

import com.slowfit.slowfit.domain.post.entity.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PostRequestDto {

    @NotNull
    private BoardType boardType;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String pace;

    private String courseUrl;

    private String imageUrl;
}
