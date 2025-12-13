package cloudcomputing.wordtreasure.api.token.response;

import cloudcomputing.wordtreasure.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenErrorCode implements ErrorCode {
    INSUFFICIENT_TOKEN("TK-0001"),
    INVALID_AMOUNT("TK-0002"),
    GAME_SESSION_REQUIRED("TK-0003"),
    MEMBER_NOT_FOUND("TK-0004"),
    ;

    private final String value;
}
