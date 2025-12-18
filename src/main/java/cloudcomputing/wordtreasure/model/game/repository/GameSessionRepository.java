package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    /**
     * 특정 회원의 특정 일일 단어에 대한 게임 세션 조회
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.member.memberId = :memberId AND gs.dailyWord.id = :dailyWordId")
    Optional<GameSession> findByMemberIdAndDailyWordId(
            @Param("memberId") Long memberId,
            @Param("dailyWordId") Long dailyWordId
    );

    /**
     * Pessimistic Write Lock을 사용한 조회
     * - 다른 트랜잭션이 동일한 레코드를 수정하지 못하도록 차단
     * - SELECT ... FOR UPDATE 쿼리 실행
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT gs FROM GameSession gs " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.dailyWord.id = :dailyWordId")
    Optional<GameSession> findByMemberIdAndDailyWordIdWithLock(
            @Param("memberId") Long memberId,
            @Param("dailyWordId") Long dailyWordId
    );

    /**
     * 여러 회원의 게임 세션 일괄 조회 (실시간 순위용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.member " +
            "WHERE gs.member.memberId IN :memberIds AND gs.dailyWord.id = :dailyWordId")
    List<GameSession> findByMemberIdsAndDailyWordId(
            @Param("memberIds") List<Long> memberIds,
            @Param("dailyWordId") Long dailyWordId
    );

    /**
     * 특정 회원의 최근 게임 세션 조회 (최신순, 완료된 것만)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.status IN ('SUCCESS', 'FAIL') " +
            "ORDER BY gs.completedAt DESC")
    List<GameSession> findRecentCompletedGamesByMemberId(
            @Param("memberId") Long memberId
    );

    /**
     * 특정 회원의 진행 중인 게임 세션 조회
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.member.memberId = :memberId AND gs.status = 'PLAYING'")
    Optional<GameSession> findPlayingSessionByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 일일 단어의 모든 성공한 게임 세션 조회 (순위별)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "WHERE gs.dailyWord.id = :dailyWordId AND gs.status = 'SUCCESS' " +
            "ORDER BY gs.attemptCount ASC, gs.completedAt ASC")
    List<GameSession> findSuccessfulSessionsByDailyWordId(@Param("dailyWordId") Long dailyWordId);

    /**
     * 특정 회원이 오늘 게임을 시작했는지 확인
     */
    @Query("SELECT COUNT(gs) > 0 FROM GameSession gs " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.dailyWord.gameDate = :gameDate")
    boolean existsByMemberIdAndGameDate(
            @Param("memberId") Long memberId,
            @Param("gameDate") LocalDate gameDate
    );

    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.member " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.dailyWord.gameDate = :date " +
            "ORDER BY gs.highestSimilarity DESC NULLS LAST, " +
            "gs.attemptCount ASC, " +
            "COALESCE(gs.completedAt, gs.updatedAt) ASC")
    Page<GameSession> findDailyLeaderboard(
            @Param("date") LocalDate date,
            Pageable pageable
    );

    /**
     * 일간 특정 회원의 게임 세션 조회 (일간 순위용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.member " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.dailyWord.gameDate = :date")
    Optional<GameSession> findDailySessionByMemberId(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );

    /**
     * 일간 전체 성공자 수 조회
     */
    @Query("SELECT COUNT(gs) FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate = :date ")
    long countDailyParticipants(@Param("date") LocalDate date);

    /**
     * 기간별 리더보드 조회 (누적 토큰 기준)
     */
    @Query(value = "SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
            "SUM(gs.attemptCount) as totalAttempts, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate BETWEEN :startDate AND :endDate " +
            "GROUP BY gs.member.memberId, gs.member.nickName " +
            "ORDER BY SUM(gs.tokensEarned) DESC, " +
            "SUM(gs.attemptCount) ASC, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) ASC",
            countQuery = "SELECT COUNT(DISTINCT gs.member.memberId) " +
                    "FROM GameSession gs " +
                    "WHERE gs.dailyWord.gameDate BETWEEN :startDate AND :endDate"
    )
    Page<LeaderboardProjection> findPeriodLeaderboard(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * 기간별 특정 회원의 집계 데이터 조회
     */
    @Query("SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
            "SUM(gs.attemptCount) as totalAttempts, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.dailyWord.gameDate BETWEEN :startDate AND :endDate " +
            "GROUP BY gs.member.memberId, gs.member.nickName")
    Optional<LeaderboardProjection> findPeriodStatsByMemberId(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 기간별 전체 참여자 수 조회 (중복 제거)
     */
    @Query("SELECT COUNT(DISTINCT gs.member.memberId) FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate BETWEEN :startDate AND :endDate")
    long countPeriodParticipants(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 전체 기간 리더보드 조회
     */
    @Query("SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
            "SUM(gs.attemptCount) as totalAttempts, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "GROUP BY gs.member.memberId, gs.member.nickName " +
            "ORDER BY SUM(gs.tokensEarned) DESC, " +
            "SUM(gs.attemptCount) ASC, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) ASC")
    Page<LeaderboardProjection> findAllTimeLeaderboard(Pageable pageable);

    /**
     * 전체 기간 특정 회원의 집계 데이터 조회
     */
    @Query("SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
            "SUM(gs.attemptCount) as totalAttempts, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "WHERE gs.member.memberId = :memberId " +
            "GROUP BY gs.member.memberId, gs.member.nickName")
    Optional<LeaderboardProjection> findAllTimeStatsByMemberId(@Param("memberId") Long memberId);

    /**
     * 전체 참여자 수 조회
     */
    @Query("SELECT COUNT(DISTINCT gs.member.memberId) FROM GameSession gs")
    long countAllParticipants();

    /**
     * 특정 날짜의 평균 시도 횟수 계산
     */
    @Query("SELECT COALESCE(AVG(gs.attemptCount), 0) FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate = :date " +
            "AND gs.status = 'SUCCESS'")
    Double calculateAverageAttempts(@Param("date") LocalDate date);

    /**
     * 회원의 날짜 범위 내 게임 세션 조회 (활동 캘린더용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.member.memberId = :memberId " +
            "AND gs.dailyWord.gameDate BETWEEN :startDate AND :endDate " +
            "ORDER BY gs.startedAt ASC")
    List<GameSession> findByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 회원의 특정 상태 게임 세션 조회 (최고 기록용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.member.memberId = :memberId AND gs.status = :status")
    List<GameSession> findByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") GameStatus status
    );

    /**
     * 회원의 모든 게임 세션 시간순 조회 (연속 기록용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.member.memberId = :memberId " +
            "ORDER BY gs.startedAt ASC")
    List<GameSession> findByMemberIdOrderByStartedAt(@Param("memberId") Long memberId);

    /**
     * 회원의 모든 게임 세션 조회 (첫 시도 유사도용)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.member.memberId = :memberId")
    List<GameSession> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 자정 정리용: 특정 gameDate의 PLAYING 세션을 일괄 만료 처리
     * - 통계/리포트에서 PLAYING 잔존 방지
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE GameSession gs
               SET gs.status = :toStatus,
                   gs.completedAt = :completedAt
             WHERE gs.status = :fromStatus
               AND gs.dailyWord.gameDate = :gameDate
            """)
    int expireSessionsByGameDateAndStatus(
            @Param("gameDate") LocalDate gameDate,
            @Param("fromStatus") GameStatus fromStatus,
            @Param("toStatus") GameStatus toStatus,
            @Param("completedAt") LocalDateTime completedAt
    );

    /**
     * 자정 정리용: 특정 날짜(gameDate)의 PLAYING 세션 목록 조회 (연관 엔티티 fetch)
     */
    @Query("""
            SELECT gs
              FROM GameSession gs
              JOIN FETCH gs.member
              JOIN FETCH gs.dailyWord
             WHERE gs.dailyWord.gameDate = :gameDate
               AND gs.status = :status
            """)
    List<GameSession> findByGameDateAndStatusWithFetch(
            @Param("gameDate") LocalDate gameDate,
            @Param("status") GameStatus status
    );

    /**
     * 특정 날짜의 성공한 게임 세션 조회 (순위순)
     * - 토큰 풀 배분용
     * - finalRank가 낮은 순서대로 정렬 (1위, 2위, 3위...)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.member " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.dailyWord.gameDate = :date " +
            "AND gs.status = 'SUCCESS' " +
            "AND gs.finalRank IS NOT NULL " +
            "ORDER BY gs.finalRank ASC")
    List<GameSession> findSuccessSessionsByDateOrderByRank(@Param("date") LocalDate date);
}
