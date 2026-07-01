package com.slowfit.slowfit.domain.comment.service;

import com.slowfit.slowfit.domain.comment.dto.CommentRequestDto;
import com.slowfit.slowfit.domain.comment.dto.CommentResponseDto;
import com.slowfit.slowfit.domain.comment.entitiy.Comment;
import com.slowfit.slowfit.domain.comment.repository.CommentRepository;
import com.slowfit.slowfit.domain.post.entity.Post;
import com.slowfit.slowfit.domain.post.repository.PostRepository;
import com.slowfit.slowfit.domain.user.entitiy.Role;
import com.slowfit.slowfit.domain.user.entitiy.User;
import com.slowfit.slowfit.domain.user.repository.UserRepository;
import com.slowfit.slowfit.global.service.TextModerationService;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TextModerationService textModerationService;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository,
                          TextModerationService textModerationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.textModerationService = textModerationService;
    }

    public CommentResponseDto createComment(@NonNull Long postId, @NonNull CommentRequestDto requestDto) {
        Post post = findPostById(postId);
        User writer = findAuthenticatedUser();
        User mentionUser = resolveMentionUser(requestDto.getMentionUsername());

        Boolean contentFlagged = textModerationService.moderateTextAsync(requestDto.getContent()).join();
        if (Boolean.TRUE.equals(contentFlagged)) {
            throw new IllegalArgumentException("댓글에 부적절한 텍스트가 포함되어 있습니다.");
        }

        Comment comment = Objects.requireNonNull(Comment.builder()
            .post(post)
            .user(writer)
            .mentionUser(mentionUser)
            .content(requestDto.getContent())
            .build());

        return mapToResponseDto(commentRepository.save(comment));
    }

    public List<CommentResponseDto> getCommentsByPostId(@NonNull Long postId) {
        return commentRepository.findByPostId(postId).stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());
    }

    public void deleteComment(@NonNull Long commentId) {
        Comment comment = findCommentById(commentId);
        validateCommentOwner(comment);
        commentRepository.delete(comment);
    }

    private @NonNull Comment findCommentById(@NonNull Long commentId) {
        return Objects.requireNonNull(commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("Comment not found.")));
    }

    private @NonNull Post findPostById(@NonNull Long postId) {
        return Objects.requireNonNull(postRepository.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("Post not found.")));
    }

    private User resolveMentionUser(String mentionUsername) {
        if (mentionUsername == null || mentionUsername.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(mentionUsername)
            .orElseThrow(() -> new IllegalArgumentException("Mention user not found."));
    }

    private User findAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }

    private void validateCommentOwner(Comment comment) {
        User currentUser = findAuthenticatedUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        if (!comment.getUser().getUsername().equals(currentUser.getUsername())) {
            throw new AccessDeniedException("You are not authorized to delete this comment.");
        }
    }

    private CommentResponseDto mapToResponseDto(Comment comment) {
        return CommentResponseDto.builder()
            .id(comment.getId())
            .username(comment.getUser().getUsername())
            .mentionUsername(comment.getMentionUser() != null ? comment.getMentionUser().getUsername() : null)
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}
