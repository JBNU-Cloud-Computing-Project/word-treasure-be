package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.member.entity.Member;

import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        String email,
        String nickname,
        Integer currentTokens,
        Long totalTokensEarned,
        LocalDateTime lastLoginAt
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getNickName(),
                member.getCurrentTokens(),
                member.getTotalTokensEarned(),
                member.getLastLoginAt()
        );
    }
}
