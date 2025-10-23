package com.yushan.user_service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.user_service.service.MailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RedisUtil unit tests
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=",
        "spring.kafka.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "jwt.secret=test-secret-key-for-integration-tests-123456",
        "jwt.access-token.expiration=3600000",
        "jwt.refresh-token.expiration=86400000"
})
public class RedisUtilTest {

    private RedisUtil redisUtil;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @MockBean
    private MailService mailService;
    @MockBean
    private MailUtil mailUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        redisUtil = new RedisUtil(stringRedisTemplate, objectMapper);
    }

    @AfterEach
    void tearDown() {
        // clear mock
        reset(stringRedisTemplate, objectMapper, valueOperations, zSetOperations);
    }

    // TC1: setJson normal case
    @Test
    void testSetJson_Normal() throws Exception {
        String key = "testKey";
        Object value = "testValue";
        long timeout = 10L;
        TimeUnit unit = TimeUnit.SECONDS;
        String json = "\"testValue\"";

        when(objectMapper.writeValueAsString(value)).thenReturn(json);

        redisUtil.setJson(key, value, timeout, unit);

        verify(valueOperations).set(key, json, timeout, unit);
    }

    // TC2: setJson failed
    @Test
    void testSetJson_SerializationException() throws Exception {
        String key = "testKey";
        Object value = new Object();
        long timeout = 10L;
        TimeUnit unit = TimeUnit.SECONDS;

        when(objectMapper.writeValueAsString(value)).thenThrow(new JsonProcessingException("serialization failed") {});

        redisUtil.setJson(key, value, timeout, unit);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    // TC3: getJson normal case
    @Test
    void testGetJson_Normal() throws Exception {
        String key = "testKey";
        String json = "\"testValue\"";
        String value = "testValue";

        when(valueOperations.get(key)).thenReturn(json);
        when(objectMapper.readValue(json, String.class)).thenReturn(value);

        String result = redisUtil.getJson(key, String.class);

        assertEquals(value, result);
    }

    // TC4: getJson key don't exist
    @Test
    void testGetJson_KeyNotFound() {
        String key = "nonExistentKey";

        when(valueOperations.get(key)).thenReturn(null);

        String result = redisUtil.getJson(key, String.class);

        assertNull(result);
    }

    // TC5: getJson deserialization failed
    @Test
    void testGetJson_DeserializationException() throws Exception {
        String key = "testKey";
        String json = "invalidJson";

        when(valueOperations.get(key)).thenReturn(json);
        when(objectMapper.readValue(json, String.class)).thenThrow(new JsonProcessingException("deserialization failed") {});

        String result = redisUtil.getJson(key, String.class);

        assertNull(result);
    }

    @Test
    void testSet_withTimeout() {
        redisUtil.set("key", "value", 10, TimeUnit.SECONDS);
        verify(valueOperations).set("key", "value", 10, TimeUnit.SECONDS);
    }

    @Test
    void testSet_withoutTimeout() {
        redisUtil.set("key", "value");
        verify(valueOperations).set("key", "value");
    }

    @Test
    void testGet() {
        when(valueOperations.get("key")).thenReturn("value");
        assertEquals("value", redisUtil.get("key"));
    }

    @Test
    void testHasKey() {
        when(stringRedisTemplate.hasKey("key")).thenReturn(true);
        assertTrue(redisUtil.hasKey("key"));

        when(stringRedisTemplate.hasKey("non-key")).thenReturn(false);
        assertFalse(redisUtil.hasKey("non-key"));

        when(stringRedisTemplate.hasKey("null-key")).thenReturn(null);
        assertFalse(redisUtil.hasKey("null-key"));
    }

    @Test
    void testExpire() {
        redisUtil.expire("key", 10, TimeUnit.SECONDS);
        verify(stringRedisTemplate).expire("key", 10, TimeUnit.SECONDS);
    }

    @Test
    void testGetExpire() {
        when(stringRedisTemplate.getExpire("key", TimeUnit.SECONDS)).thenReturn(10L);
        assertEquals(10L, redisUtil.getExpire("key", TimeUnit.SECONDS));
    }

    @Test
    void testDelete_singleKey() {
        redisUtil.delete("key");
        verify(stringRedisTemplate).delete("key");
    }

    @Test
    void testDelete_multipleKeys() {
        Collection<String> keys = Arrays.asList("key1", "key2");
        redisUtil.delete(keys);
        verify(stringRedisTemplate).delete(keys);
    }

    @Test
    void testIncr() {
        when(valueOperations.increment("key")).thenReturn(1L);
        assertEquals(1L, redisUtil.incr("key"));
    }

    @Test
    void testDecr() {
        when(valueOperations.decrement("key")).thenReturn(-1L);
        assertEquals(-1L, redisUtil.decr("key"));
    }

    @Test
    void testZAdd() {
        redisUtil.zAdd("zkey", "member", 1.0);
        verify(zSetOperations).add("zkey", "member", 1.0);
    }

    @Test
    void testZReverseRange() {
        Set<String> expected = new HashSet<>(Arrays.asList("m1", "m2"));
        when(zSetOperations.reverseRange("zkey", 0, 1)).thenReturn(expected);
        assertEquals(expected, redisUtil.zReverseRange("zkey", 0, 1));
    }

    @Test
    void testZCard() {
        when(zSetOperations.zCard("zkey")).thenReturn(5L);
        assertEquals(5L, redisUtil.zCard("zkey"));
    }

    @Test
    void testZReverseRank() {
        when(zSetOperations.reverseRank("zkey", "member")).thenReturn(2L);
        assertEquals(2L, redisUtil.zReverseRank("zkey", "member"));
    }

    @Test
    void testZScore() {
        when(zSetOperations.score("zkey", "member")).thenReturn(99.5);
        assertEquals(99.5, redisUtil.zScore("zkey", "member"));
    }

    @Test
    void testKeys() {
        Set<String> expected = new HashSet<>(Arrays.asList("key1", "key2"));
        when(stringRedisTemplate.keys("key*")).thenReturn(expected);
        assertEquals(expected, redisUtil.keys("key*"));
    }
}
