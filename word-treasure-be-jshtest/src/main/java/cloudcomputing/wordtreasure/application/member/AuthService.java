package cloudcomputing.wordtreasure.application.member;

import cloudcomputing.wordtreasure.domain.member.*;
import cloudcomputing.wordtreasure.controller.member.dto.*;
import cloudcomputing.wordtreasure.domain.token.TokenTransactions;
import cloudcomputing.wordtreasure.application.token.TokenService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MembersRepository membersRepository;
    private final HttpSession session;
    private final TokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .currentTokens(0)
                .totalTokensEarned(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 1) DB 저장
        Members saved = membersRepository.save(member);

        // 2) 회원가입 보너스 50 토큰 지급
        tokenService.addTokens(
                saved.getMemberId(),
                50,
                TokenTransactions.TransactionType.SIGNUP_BONUS,
                "회원가입 보너스"
        );

        return saved;
    }

    public Members login(LoginRequest req) {
        Members member = membersRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(req.getPassword(), member.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 세션 생성
        session.setAttribute("memberId", member.getMemberId());

        member.setLastLoginAt(LocalDateTime.now());
        membersRepository.save(member);

        return member;
    }

    public void logout() {
        session.invalidate();
    }
}
