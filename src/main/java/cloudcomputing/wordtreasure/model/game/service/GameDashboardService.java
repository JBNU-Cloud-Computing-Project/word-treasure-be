package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.*;
import cloudcomputing.wordtreasure.model.game.entity.*;
import cloudcomputing.wordtreasure.model.game.repository.AttemptRepository;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.ExtraHintRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.entity.MemberStatistics;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberStatisticsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameDashboardService {
    private final MemberRepository memberRepository;
    private final MemberStatisticsRepository statisticsRepository;
    private final GameSessionRepository gameSessionRepository;
    private final DailyWordRepository dailyWordRepository;
    private final AttemptRepository attemptRepository;
    private final ExtraHintRepository extraHintRepository;
    private final GameConfigService gameConfigService;
    private final EntityManager entityManager;

    /**
     * 현재 게임 상태 조회
     */
    public CurrentGameInfo getCurrentGameInfo(Long memberId) {
        log.info("현재 게임 상태 조회 - memberId: {}", memberId);

        // 1. 오늘의 일일 단어 조회
        entityManager.clear();
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDateTime startOfToday = LocalDate.now(seoulZone).atStartOfDay();

        log.info("오늘 자정(서울): {}", startOfToday);

        DailyWord todayWord = dailyWordRepository.findTodayWordByCreatedAt(startOfToday)
                .orElseThrow(() -> new IllegalStateException("오늘의 단어가 등록되지 않았습니다."));

        log.info("✅ 조회된 단어: id={}, gameDate={}, word={}, createdAt={}",
                todayWord.getId(),
                todayWord.getGameDate(),
                todayWord.getWord(),
                todayWord.getCreatedAt());
/*
        log.info("DB 조회 시작 - findTodayWord({})", today);
        Optional<DailyWord> wordOpt = dailyWordRepository.findTodayWord(today);
        log.info("DB 조회 결과 - isPresent: {}", wordOpt.isPresent());

        DailyWord todayWord = dailyWordRepository.findTodayWord(today)
                .orElseThrow(() -> new IllegalStateException("오늘의 단어가 등록되지 않았습니다."));

        log.info("✅ 조회된 단어: gameDate={}, word={}",
                todayWord.getGameDate(), todayWord.getWord());*/
        // 2. 현재 회원의 게임 세션 조회
        Optional<GameSession> sessionOpt = gameSessionRepository
                .findByMemberIdAndDailyWordId(memberId, todayWord.getId());

        // 3. 남은 시간 계산 (자정까지)
        String remainingTime = calculateRemainingTime();

        // 4. 게임 세션이 없으면 (게임 시작 전)
        if (sessionOpt.isEmpty()) {
            return CurrentGameInfo.withoutProgress(
                    todayWord.getId(),
                    todayWord.getGameDate(),
                    remainingTime,
                    todayWord.getDifficulty()
            );
        }

        // 5. 게임 세션이 있으면 (게임 진행 중 or 완료)
        GameSession session = sessionOpt.get();

        // 6. 진행 상태 조회
        GameProgressDto progress = getGameProgress(session);

        return CurrentGameInfo.withProgress(
                todayWord.getId(),
                todayWord.getGameDate(),
                session.getStatus(),
                remainingTime,
                todayWord.getDifficulty(),
                session.getId(),
                progress
        );
    }

    /**
     * 사용자 통계 조회
     */
    public UserStatisticsInfo getUserStatistics(Long memberId) {
        log.info("사용자 통계 조회 - memberId: {}", memberId);

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 통계 조회 (없으면 기본값 생성)
        MemberStatistics statistics = statisticsRepository.findByMemberId(memberId)
                .orElse(createDefaultStatistics(member));

        return UserStatisticsInfo.from(statistics, member.getCurrentTokens());
    }

    /**
     * 최근 게임 기록 조회
     */
    public List<RecentGameInfo> getRecentGames(Long memberId, int limit) {
        log.info("최근 게임 기록 조회 - memberId: {}, limit: {}", memberId, limit);

        List<GameSession> recentSessions = gameSessionRepository
                .findRecentCompletedGamesByMemberId(memberId);

        return recentSessions.stream()
                .limit(limit)
                .map(RecentGameInfo::from)
                .toList();
    }

    /**
     * 자정까지 남은 시간 계산 (HH:MM:SS 형식)
     */
    private String calculateRemainingTime() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(seoulZone);
        ZonedDateTime midnight = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(seoulZone);

        Duration duration = Duration.between(now, midnight);

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 기본 통계 객체 생성
     */
    private MemberStatistics createDefaultStatistics(Member member) {
        log.info("기본 통계 생성 - memberId: {}", member.getMemberId());
        return new MemberStatistics(
                member.getMemberId(),
                member,
                0, 0, 0,
                java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO,
                null, null,
                0, 0,
                null
        );
    }

    /**
     * 게임 진행 상태 조회
     */
    private GameProgressDto getGameProgress(GameSession session) {
        Long gameSessionId = session.getId();

        log.info("게임 진행 상태 조회 - gameSessionId: {}", gameSessionId);

        // 1. 모든 시도 기록 조회
        List<Attempt> attempts = attemptRepository
                .findByGameSessionIdOrderByAttemptNumber(gameSessionId);

        // 2. 모든 힌트 기록 조회
        List<ExtraHint> hints = extraHintRepository
                .findByGameSessionIdOrderByCreatedAt(gameSessionId);

        // 3. 시도와 힌트의 총 토큰 사용량 계산
        int totalTokensUsed = session.getTokensSpent();

        // 4. 최대 시도 횟수
        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);

        // 5. DTO 변환
        List<AttemptDto> attemptDtos = attempts.stream()
                .map(AttemptDto::from)
                .toList();

        List<HintDto> hintDtos = hints.stream()
                .map(HintDto::from)
                .toList();

        log.info("진행 상태 조회 완료 - attempts: {}, hints: {}, tokensUsed: {}",
                attemptDtos.size(), hintDtos.size(), totalTokensUsed);

        return GameProgressDto.from(
                attemptDtos,
                hintDtos,
                maxAttempts,
                session.getAttemptCount(),
                totalTokensUsed
        );
    }
}
