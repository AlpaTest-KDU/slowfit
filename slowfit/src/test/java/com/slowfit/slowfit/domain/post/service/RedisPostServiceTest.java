package com.slowfit.slowfit.domain.post.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.slowfit.slowfit.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisPostServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private RedisPostService redisPostService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assertNotNull(postRepository);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
        redisPostService = new RedisPostService(stringRedisTemplate, postRepository);
    }

    @Test
    void increaseViewCountShouldIncrementRedisValue() {
        when(valueOperations.increment("post:viewCount:1", 1L)).thenReturn(3L);

        redisPostService.increaseViewCount(1L);

        verify(valueOperations).increment("post:viewCount:1", 1L);
    }

    @Test
    void toggleLikeShouldAddAndRemoveLike() {
        when(setOperations.isMember("post:likeUsers:1", "user-1")).thenReturn(false, true);
        when(setOperations.add("post:likeUsers:1", "user-1")).thenReturn(1L);
        when(valueOperations.increment("post:likeCount:1", 1L)).thenReturn(1L);
        when(valueOperations.increment("post:likeCount:1", -1L)).thenReturn(0L);

        assertTrue(redisPostService.toggleLike(1L, "user-1"));
        assertFalse(redisPostService.toggleLike(1L, "user-1"));

        verify(setOperations).add("post:likeUsers:1", "user-1");
        verify(setOperations).remove("post:likeUsers:1", "user-1");
    }
}
