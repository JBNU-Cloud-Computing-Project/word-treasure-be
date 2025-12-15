package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    /**
     * 일간 리더보드 조회 (시도횟수 + 완료시간 순)
     */
    @Query("SELECT gs FROM GameSession gs " +
            "JOIN FETCH gs.member " +
            "JOIN FETCH gs.dailyWord " +
            "WHERE gs.dailyWord.gameDate = :date " +
            "AND gs.status = 'SUCCESS' " +
            "ORDER BY gs.attemptCount ASC, gs.completedAt ASC")
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
            "AND gs.dailyWord.gameDate = :date " +
            "AND gs.status = 'SUCCESS'")
    Optional<GameSession> findDailySessionByMemberId(
            @Param("memberId") Long memberId,
            @Param("date") LocalDate date
    );

    /**
     * 일간 전체 성공자 수 조회
     */
    @Query("SELECT COUNT(gs) FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate = :date " +
            "AND gs.status = 'SUCCESS'")
    long countDailySuccessful(@Param("date") LocalDate date);

    /**
     * 기간별 리더보드 조회 (누적 토큰 기준)
     */
    @Query(value = "SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "WHERE gs.dailyWord.gameDate BETWEEN :startDate AND :endDate " +
            "GROUP BY gs.member.memberId, gs.member.nickName " +
            "ORDER BY SUM(gs.tokensEarned) DESC",
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
            "AVG(CASE WHEN gs.status = 'SUCCESS' " +
            "  THEN timestampdiff(second, gs.startedAt, gs.completedAt) " +
            "  ELSE NULL END) as avgCompletionSeconds " +
            "FROM GameSession gs " +
            "GROUP BY gs.member.memberId, gs.member.nickName " +
            "ORDER BY tokensEarned DESC")
    Page<LeaderboardProjection> findAllTimeLeaderboard(Pageable pageable);

    /**
     * 전체 기간 특정 회원의 집계 데이터 조회
     */
    @Query("SELECT gs.member.memberId as memberId, " +
            "gs.member.nickName as nickname, " +
            "COUNT(gs) as totalGames, " +
            "SUM(CASE WHEN gs.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulGames, " +
            "SUM(gs.tokensEarned) as tokensEarned, " +
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

}
