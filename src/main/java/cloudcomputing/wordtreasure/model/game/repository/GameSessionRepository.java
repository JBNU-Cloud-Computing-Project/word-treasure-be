package cloudcomputing.wordtreasure.model.game.repository;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
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
     * 특정 일일 단어의 모든 게임 세션 조회 (순위용)
     */
    @Query("SELECT gs FROM GameSession gs WHERE gs.dailyWord.id = :dailyWordId AND gs.status = 'SUCCESS' ORDER BY gs.completedAt ASC")
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
}
