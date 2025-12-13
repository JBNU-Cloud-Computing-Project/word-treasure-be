package cloudcomputing.wordtreasure.model.member.repositroy;

import cloudcomputing.wordtreasure.model.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 비관적 락을 사용한 회원 조회
     * 토큰 업데이트 시 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId")
    Optional<Member> findByIdWithLock(@Param("memberId") Long memberId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickName(String nickName);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);
}
