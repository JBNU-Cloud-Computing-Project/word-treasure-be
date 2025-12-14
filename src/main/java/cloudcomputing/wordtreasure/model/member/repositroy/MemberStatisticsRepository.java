package cloudcomputing.wordtreasure.model.member.repositroy;

import cloudcomputing.wordtreasure.model.member.entity.MemberStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberStatisticsRepository extends JpaRepository<MemberStatistics, Long> {
    /**
     * 회원 ID로 통계 조회
     */
    @Query("SELECT ms FROM MemberStatistics ms WHERE ms.member.memberId = :memberId")
    Optional<MemberStatistics> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 회원 ID로 통계 존재 여부 확인
     */
    @Query("SELECT COUNT(ms) > 0 FROM MemberStatistics ms WHERE ms.member.memberId = :memberId")
    boolean existsByMemberId(@Param("memberId") Long memberId);
}
