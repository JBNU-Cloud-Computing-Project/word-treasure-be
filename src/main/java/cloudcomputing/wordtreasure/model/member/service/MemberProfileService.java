package cloudcomputing.wordtreasure.model.member.service;

import cloudcomputing.wordtreasure.model.member.dto.ProfileInfo;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 프로필 정보 조회
     *
     * @param memberId 회원 ID
     * @return 프로필 정보
     */
    public ProfileInfo getProfile(Long memberId) {
        log.info("프로필 정보 조회 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return new ProfileInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getNickName(),
                member.getCurrentTokens(),
                member.getTotalTokensEarned(),
                member.getCreatedAt(),
                member.getLastLoginAt()
        );
    }

    /**
     * 프로필 수정
     *
     * @param memberId    회원 ID
     * @param newNickname 새 닉네임
     * @return 수정된 프로필 정보
     */
    @Transactional
    public ProfileInfo updateProfile(Long memberId, String newNickname) {
        log.info("프로필 수정 요청 - memberId: {}, newNickname: {}", memberId, newNickname);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 닉네임 중복 체크
        if (memberRepository.existsByNickName(newNickname) &&
                !member.getNickName().equals(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 닉네임 변경
        try {
            var field = Member.class.getDeclaredField("nickName");
            field.setAccessible(true);
            field.set(member, newNickname);
        } catch (Exception e) {
            log.error("닉네임 변경 실패", e);
            throw new RuntimeException("닉네임 변경 중 오류 발생", e);
        }

        log.info("프로필 수정 완료 - memberId: {}, newNickname: {}", memberId, newNickname);

        return new ProfileInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getNickName(),
                member.getCurrentTokens(),
                member.getTotalTokensEarned(),
                member.getCreatedAt(),
                member.getLastLoginAt()
        );
    }

    /**
     * 비밀번호 변경
     *
     * @param memberId        회원 ID
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     */
    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        log.info("비밀번호 변경 요청 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 현재 비밀번호 확인 (BCrypt 검증)
        if (!passwordEncoder.matches(currentPassword, member.getPasswordHash())) {
            log.warn("비밀번호 변경 실패 - 현재 비밀번호 불일치: memberId={}", memberId);
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 새 비밀번호로 변경
        try {
            var field = Member.class.getDeclaredField("passwordHash");
            field.setAccessible(true);
            field.set(member, encodedNewPassword);
        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            throw new RuntimeException("비밀번호 변경 중 오류 발생", e);
        }

        log.info("비밀번호 변경 완료 - memberId: {}", memberId);
    }
}
