
package cloudcomputing.wordtreasure.application.member;

import cloudcomputing.wordtreasure.domain.member.*;
import cloudcomputing.wordtreasure.controller.member.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MembersRepository membersRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Members signup(SignupRequest req) {

        if (membersRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        if (membersRepository.existsByNickName(req.getNickName())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }

        Members member = Members.builder()
                .email(req.getEmail())
                .nickName(req.getNickName())
                .passwordHash(encoder.encode(req.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return membersRepository.save(member);
    }

    public Members login(LoginRequest req) {
        Members member = membersRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!encoder.matches(req.getPassword(), member.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        member.setLastLoginAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        membersRepository.save(member);

        return member;
    }
}
