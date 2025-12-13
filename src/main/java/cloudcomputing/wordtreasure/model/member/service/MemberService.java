package cloudcomputing.wordtreasure.model.member.service;

import cloudcomputing.wordtreasure.api.member.request.LoginRequest;
import cloudcomputing.wordtreasure.api.member.request.SignupRequest;
import cloudcomputing.wordtreasure.model.game.entity.GameConfigKey;
import cloudcomputing.wordtreasure.model.game.service.GameConfigService;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import cloudcomputing.wordtreasure.model.token.entity.TransactionType;
import cloudcomputing.wordtreasure.model.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final GameConfigService gameConfigService;

    /**
     * 회원가입
     * - 비밀번호 BCrypt 암호화
     * - 가입 보너스 토큰 지급
     */
    @Transactional
    public Member signup(SignupRequest request) {
        log.info("회원가입 시도 - email: {}, nickname: {}", request.email(), request.nickName());

        // 1. 중복 검증
        validateDuplicateEmail(request.email());
        validateDuplicateNickname(request.nickName());

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. 회원 생성
        Member member = Member.builder()
                .email(request.email())
                .nickName(request.nickName())
                .passwordHash(encodedPassword)
                .build();

        Member savedMember = memberRepository.save(member);

        // 4. 가입 보너스 토큰 지급
        int signupBonus = gameConfigService.getIntValue(GameConfigKey.SIGNUP_BONUS_TOKENS);
        tokenService.addTokens(
                savedMember.getMemberId(),
                signupBonus,
                TransactionType.SIGNUP_BONUS,
                "회원가입 축하 보너스",
                null
        );

        log.info("회원가입 완료 - memberId: {}, bonusTokens: {}", savedMember.getMemberId(), signupBonus);

        return savedMember;
    }

    /**
     * 로그인
     * - 비밀번호 검증
     * - 마지막 로그인 시간 업데이트
     */
    @Transactional
    public Member login(LoginRequest request) {
        log.info("로그인 시도 - email: {}", request.email());

        // 1. 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("로그인 실패 - 존재하지 않는 이메일: {}", request.email());
                    return new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            log.warn("로그인 실패 - 비밀번호 불일치: {}", request.email());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 마지막 로그인 시간 업데이트
        member.updateLastLoginAt();

        log.info("로그인 성공 - memberId: {}", member.getMemberId());

        return member;
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameDuplicate(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    /**
     * 회원 조회
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            log.warn("이메일 중복 - email: {}", email);
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    private void validateDuplicateNickname(String nickName) {
        if (memberRepository.existsByNickName(nickName)) {
            log.warn("닉네임 중복 - nickname: {}", nickName);
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }
}
