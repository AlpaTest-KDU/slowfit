package com.slowfit.slowfit.domain.post.service;

import com.slowfit.slowfit.domain.post.dto.PostRequestDto;
import com.slowfit.slowfit.domain.post.dto.PostResponseDto;
import com.slowfit.slowfit.domain.post.entity.BoardType;
import com.slowfit.slowfit.domain.post.entity.Post;
import com.slowfit.slowfit.domain.post.repository.PostRepository;
import com.slowfit.slowfit.domain.user.entitiy.Role;
import com.slowfit.slowfit.domain.user.entitiy.User;
import com.slowfit.slowfit.domain.user.repository.UserRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final RedisPostService redisPostService;

    public PostService(PostRepository postRepository, UserRepository userRepository, RedisPostService redisPostService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.redisPostService = redisPostService;
    }

    public PostResponseDto createPost(@NonNull PostRequestDto requestDto) {
        User user = findAuthenticatedUser();

        Post post = Objects.requireNonNull(Post.builder()
            .user(user)
            .boardType(requestDto.getBoardType())
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .pace(requestDto.getPace())
            .courseUrl(requestDto.getBoardType() == BoardType.JOGGING ? requestDto.getCourseUrl() : null)
            .imageUrl(requestDto.getImageUrl())
            .build());

        Post savedPost = postRepository.save(post);
        return mapToResponseDto(savedPost);
    }

    public Page<PostResponseDto> getAllPosts(@NonNull Pageable pageable) {
        return postRepository.findAll(pageable)
            .map(this::mapToResponseDto);
    }

    public Page<PostResponseDto> getPostsByBoardType(BoardType boardType, @NonNull Pageable pageable) {
        return postRepository.findByBoardType(boardType, pageable)
            .map(this::mapToResponseDto);
    }

    public PostResponseDto getPost(@NonNull Long postId) {
        redisPostService.increaseViewCount(postId);
        return mapToResponseDto(findPostById(postId));
    }

    public PostResponseDto updatePost(@NonNull Long postId, @NonNull PostRequestDto requestDto) {
        Post post = findPostById(postId);
        validatePostOwner(post);

        post.setBoardType(requestDto.getBoardType());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setPace(requestDto.getPace());
        post.setCourseUrl(requestDto.getBoardType() == BoardType.JOGGING ? requestDto.getCourseUrl() : null);
        post.setImageUrl(requestDto.getImageUrl());

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
        User currentUser = findAuthenticatedUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (!post.getUser().getUsername().equals(currentUser.getUsername())) {
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
            .pace(post.getPace())
            .courseUrl(post.getCourseUrl())
            .imageUrl(post.getImageUrl())
            .viewCount(post.getViewCount())
            .likeCount(post.getLikeCount())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build();
    }
}

