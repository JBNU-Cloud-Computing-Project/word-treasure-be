package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthSuccessCode implements SuccessCode {
    SIGNUP_SUCCESS("AU-S0001", "회원가입에 성공했습니다"),
    LOGIN_SUCCESS("AU-S0002", "로그인에 성공했습니다"),
    LOGOUT_SUCCESS("AU-S0003", "로그아웃에 성공했습니다"),
    EMAIL_AVAILABLE("AU-S0004", "이메일 유효성 검증"),
    NICKNAME_AVAILABLE("AU-S0005", "닉네임 유효성 검증"),
    MEMBER_INFO("AU-S0006", "멤버 정보를 조회했습니다");

    private final String value;
    private final String message;
}
