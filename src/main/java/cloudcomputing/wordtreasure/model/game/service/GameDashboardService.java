package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.CurrentGameInfo;
import cloudcomputing.wordtreasure.model.game.dto.RecentGameInfo;
import cloudcomputing.wordtreasure.model.game.dto.UserStatisticsInfo;
import cloudcomputing.wordtreasure.model.game.entity.DailyWord;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.repository.DailyWordRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.entity.MemberStatistics;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    /**
     * 현재 게임 상태 조회
     */
    public CurrentGameInfo getCurrentGameInfo(Long memberId) {
        log.info("현재 게임 상태 조회 - memberId: {}", memberId);

        // 1. 오늘의 일일 단어 조회
        DailyWord todayWord = dailyWordRepository.findTodayWord()
                .orElseThrow(() -> new IllegalStateException("오늘의 단어가 등록되지 않았습니다."));

        // 2. 현재 회원의 게임 세션 조회
        Optional<GameSession> sessionOpt = gameSessionRepository
                .findByMemberIdAndDailyWordId(memberId, todayWord.getId());

        // 3. 남은 시간 계산 (자정까지)
        String remainingTime = calculateRemainingTime();

        // 4. 게임 시작 여부
        boolean hasStarted = sessionOpt.isPresent();

        return new CurrentGameInfo(
                todayWord.getId(),
                todayWord.getGameDate(),
                sessionOpt.map(GameSession::getStatus).orElse(null),
                remainingTime,
                todayWord.getDifficulty(),
                hasStarted,
                sessionOpt.map(GameSession::getId).orElse(null)
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
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
}
