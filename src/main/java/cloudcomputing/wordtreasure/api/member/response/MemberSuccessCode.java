package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MemberSuccessCode implements SuccessCode {
    STATISTICS_INFO("MB-S0001", "사용자 통계를 조회했습니다"),
    PROFILE_INFO("MB-S0002", "프로필 정보를 조회했습니다"),
    ACTIVITY_CALENDAR_INFO("MB-S0003", "활동 캘린더를 조회했습니다"),
    BEST_RECORDS_INFO("MB-S0004", "최고 기록을 조회했습니다"),
    PROFILE_UPDATED("MB-S0005", "프로필을 수정했습니다"),
    PASSWORD_CHANGED("MB-S0006", "비밀번호를 변경했습니다");

    private final String value;
    private final String message;
}
