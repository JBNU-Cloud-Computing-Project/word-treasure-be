package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.RankingEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
    private static final String RANKING_KEY_PREFIX = "ranking:daily:";
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 실시간 순위에 등록
     *
     * @param dailyWordId 일일 단어 ID
     * @param memberId    회원 ID
     * @param score       점수 (낮을수록 높은 순위)
     */
    public void addToRanking(Long dailyWordId, Long memberId, double score) {
        String key = getRankingKey(dailyWordId);

        redisTemplate.opsForZSet().add(key, memberId.toString(), score);

        log.info("Redis 순위 등록 - dailyWordId: {}, memberId: {}, score: {}",
                dailyWordId, memberId, score);
    }

    /**
     * 실시간 순위 조회 (상위 N명)
     *
     * @param dailyWordId 일일 단어 ID
     * @param limit       조회할 인원 수
     * @return 순위 목록
     */
    public List<RankingEntry> getTopRankings(Long dailyWordId, int limit) {
        String key = getRankingKey(dailyWordId);

        // 점수 오름차순으로 조회 (점수가 낮을수록 높은 순위)
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().rangeWithScores(key, 0, limit - 1);

        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<RankingEntry> rankings = getRankingEntries(tuples);

        log.info("Redis 순위 조회 - dailyWordId: {}, count: {}", dailyWordId, rankings.size());

        return rankings;
    }

    private @NonNull List<RankingEntry> getRankingEntries(Set<ZSetOperations.TypedTuple<Object>> tuples) {
        List<RankingEntry> rankings = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            String memberIdStr = (String) tuple.getValue();
            Double score = tuple.getScore();

            if (memberIdStr != null && score != null) {
                rankings.add(new RankingEntry(
                        rank++,
                        Long.parseLong(memberIdStr),
                        score
                ));
            }
        }
        return rankings;
    }

    /**
     * 특정 회원의 순위 조회
     *
     * @param dailyWordId 일일 단어 ID
     * @param memberId    회원 ID
     * @return 순위 (1-based), 없으면 null
     */
    public Integer getMemberRank(Long dailyWordId, Long memberId) {
        String key = getRankingKey(dailyWordId);

        Long rank = redisTemplate.opsForZSet().rank(key, memberId.toString());

        if (rank == null) {
            return null;
        }

        // Redis rank는 0-based이므로 1을 더함
        return rank.intValue() + 1;
    }

    /**
     * 특정 회원의 점수 조회
     *
     * @param dailyWordId 일일 단어 ID
     * @param memberId    회원 ID
     * @return 점수, 없으면 null
     */
    public Double getMemberScore(Long dailyWordId, Long memberId) {
        String key = getRankingKey(dailyWordId);

        return redisTemplate.opsForZSet().score(key, memberId.toString());
    }

    /**
     * 전체 참여자 수 조회
     *
     * @param dailyWordId 일일 단어 ID
     * @return 참여자 수
     */
    public Long getTotalParticipants(Long dailyWordId) {
        String key = getRankingKey(dailyWordId);

        Long count = redisTemplate.opsForZSet().size(key);

        return count != null ? count : 0L;
    }

    /**
     * 순위 점수 계산
     * <p>
     * 점수가 낮을수록 높은 순위
     * 계산 공식: attemptCount * 1000 + completionSeconds
     * <p>
     * 예시:
     * - 3번 시도, 8분 34초 = 3000 + 514 = 3514
     * - 4번 시도, 12분 15초 = 4000 + 735 = 4735
     *
     * @param attemptCount      시도 횟수
     * @param completionSeconds 완료 시간 (초)
     * @return 순위 점수
     */
    public double calculateRankingScore(int attemptCount, long completionSeconds) {
        return attemptCount * 1000.0 + completionSeconds;
    }

    /**
     * Redis 키 생성
     */
    private String getRankingKey(Long dailyWordId) {
        return RANKING_KEY_PREFIX + dailyWordId;
    }
}
