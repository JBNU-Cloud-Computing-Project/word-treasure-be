package cloudcomputing.wordtreasure.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Redis 기반 CacheManager 설정
     * - 캐시 TTL: 5분
     * - Key: String 직렬화
     * - Value: JSON 직렬화
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))  // 5분 후 만료
                .disableCachingNullValues()       // null 값 캐싱 방지
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        // 오늘의 단어 통계 캐시 설정 (1분)
        RedisCacheConfiguration todayWordConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(1));

        // 난이도별 통계 캐시 설정 (10분)
        RedisCacheConfiguration difficultyConfig = defaultConfig
                .entryTtl(Duration.ofMinutes(10));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("todayWordStats", todayWordConfig)
                .withCacheConfiguration("difficultyStats", difficultyConfig)
                .build();
    }
}
