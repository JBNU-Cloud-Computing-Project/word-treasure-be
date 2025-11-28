
package cloudcomputing.wordtreasure.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Long> {
    Optional<Members> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickName(String nickName);
}
