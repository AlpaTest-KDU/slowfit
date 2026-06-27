package com.slowfit.slowfit.domain.post.controller;

import com.slowfit.slowfit.domain.post.dto.PostRequestDto;
import com.slowfit.slowfit.domain.post.dto.PostResponseDto;
import com.slowfit.slowfit.domain.post.entity.BoardType;
import com.slowfit.slowfit.domain.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto) {
        PostResponseDto responseDto = postService.createPost(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getPosts(@RequestParam(required = false) BoardType boardType) {
        List<PostResponseDto> posts = (boardType == null)
            ? postService.getAllPosts()
            : postService.getPostsByBoardType(boardType);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody PostRequestDto requestDto
    ) {
        return ResponseEntity.ok(postService.updatePost(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}

