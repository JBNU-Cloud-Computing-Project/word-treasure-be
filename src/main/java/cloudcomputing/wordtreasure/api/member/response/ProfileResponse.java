package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.member.dto.ProfileInfo;

import java.time.LocalDateTime;

public record ProfileResponse(
        Long memberId,
        String email,
        String nickname,
        Integer currentTokens,
        Long totalTokensEarned,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public static ProfileResponse from(ProfileInfo info) {
        return new ProfileResponse(
                info.memberId(),
                info.email(),
                info.nickname(),
                info.currentTokens(),
                info.totalTokensEarned(),
                info.createdAt(),
                info.lastLoginAt()
        );
    }
}