package com.slowfit.slowfit.domain.post.service;

import com.slowfit.slowfit.domain.post.entity.Post;
import com.slowfit.slowfit.domain.post.repository.PostRepository;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RedisPostService {

    private static final String VIEW_COUNT_KEY_PREFIX = "post:viewCount:";
    private static final String LIKE_COUNT_KEY_PREFIX = "post:likeCount:";
    private static final String LIKE_USERS_KEY_PREFIX = "post:likeUsers:";

    private final StringRedisTemplate stringRedisTemplate;
    private final PostRepository postRepository;

    public RedisPostService(StringRedisTemplate stringRedisTemplate, PostRepository postRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.postRepository = postRepository;
    }

    public void increaseViewCount(Long postId) {
        validatePostId(postId);
        stringRedisTemplate.opsForValue().increment(VIEW_COUNT_KEY_PREFIX + postId, 1);
        syncPostMetricsToDb(postId);
    }

    public boolean toggleLike(Long postId, String userId) {
        validatePostId(postId);
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        String likeUsersKey = LIKE_USERS_KEY_PREFIX + postId;
        Boolean alreadyLiked = stringRedisTemplate.opsForSet().isMember(likeUsersKey, userId);

        if (Boolean.TRUE.equals(alreadyLiked)) {
            stringRedisTemplate.opsForSet().remove(likeUsersKey, userId);
            decrementCount(LIKE_COUNT_KEY_PREFIX + postId);
            syncPostMetricsToDb(postId);
            return false;
        }

        stringRedisTemplate.opsForSet().add(likeUsersKey, userId);
        stringRedisTemplate.opsForValue().increment(LIKE_COUNT_KEY_PREFIX + postId, 1);
        syncPostMetricsToDb(postId);
        return true;
    }

    @Scheduled(fixedRate = 60000)
    public void syncPostMetricsToDbScheduled() {
        syncPostMetricsToDb();
    }

    @Async("taskExecutor")
    public void syncPostMetricsToDb() {
        Set<String> viewKeys = stringRedisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*");
        if (viewKeys != null) {
            for (String key : viewKeys) {
                Long postId = extractPostId(key, VIEW_COUNT_KEY_PREFIX);
                if (postId != null) {
                    syncSinglePostMetric(postId, key, VIEW_COUNT_KEY_PREFIX);
                }
            }
        }

        Set<String> likeKeys = stringRedisTemplate.keys(LIKE_COUNT_KEY_PREFIX + "*");
        if (likeKeys != null) {
            for (String key : likeKeys) {
                Long postId = extractPostId(key, LIKE_COUNT_KEY_PREFIX);
                if (postId != null) {
                    syncSinglePostMetric(postId, key, LIKE_COUNT_KEY_PREFIX);
                }
            }
        }
    }

    @Async("taskExecutor")
    public void syncPostMetricsToDb(Long postId) {
        validatePostId(postId);
        syncSinglePostMetric(postId, VIEW_COUNT_KEY_PREFIX + postId, VIEW_COUNT_KEY_PREFIX);
        syncSinglePostMetric(postId, LIKE_COUNT_KEY_PREFIX + postId, LIKE_COUNT_KEY_PREFIX);
    }

    private void syncSinglePostMetric(Long postId, String key, String prefix) {
        String redisValue = stringRedisTemplate.opsForValue().get(Objects.requireNonNull(key));
        if (redisValue == null || redisValue.isBlank()) {
            return;
        }

        Post post = postRepository.findById(Objects.requireNonNull(postId)).orElse(null);
        if (post == null) {
            return;
        }

        int delta = Integer.parseInt(redisValue);
        if (prefix.equals(VIEW_COUNT_KEY_PREFIX)) {
            post.setViewCount(post.getViewCount() + delta);
        } else if (prefix.equals(LIKE_COUNT_KEY_PREFIX)) {
            post.setLikeCount(post.getLikeCount() + delta);
        }

        postRepository.save(post);
        stringRedisTemplate.delete(Objects.requireNonNull(key));
    }

    private void decrementCount(String key) {
        String currentValue = stringRedisTemplate.opsForValue().get(Objects.requireNonNull(key, "key must not be null"));
        if (currentValue == null || currentValue.isBlank()) {
            stringRedisTemplate.opsForValue().set(Objects.requireNonNull(key, "key must not be null"), "0");
            return;
        }

        int current = Integer.parseInt(currentValue);
        if (current > 0) {
            stringRedisTemplate.opsForValue().increment(Objects.requireNonNull(key, "key must not be null"), -1L);
        }
    }

    private Long extractPostId(String key, String prefix) {
        try {
            if (key == null || prefix == null) {
                return null;
            }
            return Long.parseLong(key.substring(prefix.length()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("postId must be greater than 0");
        }
    }
}
