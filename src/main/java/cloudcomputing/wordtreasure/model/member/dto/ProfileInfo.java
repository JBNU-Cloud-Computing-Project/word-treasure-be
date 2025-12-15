package cloudcomputing.wordtreasure.model.member.dto;

import java.time.LocalDateTime;

public record ProfileInfo(
        Long memberId,
        String email,
        String nickname,
        Integer currentTokens,
        Long totalTokensEarned,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}
