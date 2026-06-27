package com.slowfit.slowfit.domain.post.service;

import com.slowfit.slowfit.domain.post.dto.PostRequestDto;
import com.slowfit.slowfit.domain.post.dto.PostResponseDto;
import com.slowfit.slowfit.domain.post.entity.BoardType;
import com.slowfit.slowfit.domain.post.entity.Post;
import com.slowfit.slowfit.domain.post.repository.PostRepository;
import com.slowfit.slowfit.domain.user.entitiy.User;
import com.slowfit.slowfit.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostResponseDto createPost(@NonNull PostRequestDto requestDto) {
        User user = findAuthenticatedUser();

        Post post = Objects.requireNonNull(Post.builder()
            .user(user)
            .boardType(requestDto.getBoardType())
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .build());

        Post savedPost = postRepository.save(post);
        return mapToResponseDto(savedPost);
    }

    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    public List<PostResponseDto> getPostsByBoardType(BoardType boardType) {
        return postRepository.findByBoardType(boardType).stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    public PostResponseDto getPost(@NonNull Long postId) {
        return mapToResponseDto(findPostById(postId));
    }

    public PostResponseDto updatePost(@NonNull Long postId, @NonNull PostRequestDto requestDto) {
        Post post = findPostById(postId);
        validatePostOwner(post);

        post.setBoardType(requestDto.getBoardType());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        return mapToResponseDto(post);
    }

    public void deletePost(@NonNull Long postId) {
        Post post = findPostById(postId);
        validatePostOwner(post);
        postRepository.delete(post);
    }

    private @NonNull Post findPostById(@NonNull Long postId) {
        return Objects.requireNonNull(postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found.")));
    }

    private User findAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    private void validatePostOwner(Post post) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!post.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to modify this post.");
        }
    }

    private PostResponseDto mapToResponseDto(Post post) {
        return PostResponseDto.builder()
            .id(post.getId())
            .username(post.getUser().getUsername())
            .boardType(post.getBoardType())
            .title(post.getTitle())
            .content(post.getContent())
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }
}

