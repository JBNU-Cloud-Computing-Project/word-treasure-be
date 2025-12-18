package cloudcomputing.wordtreasure.api.token.response;

import cloudcomputing.wordtreasure.common.controller.response.SuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenSuccessCode implements SuccessCode {
    TOKEN_SUCCESS_CODE("TP-S0001", "오늘의 토큰 풀을 조회했습니다"),
    ;

    private final String value;
    private final String message;
}
