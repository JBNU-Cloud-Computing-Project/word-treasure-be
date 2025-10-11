package cloudcomputing.wordtreasure.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR("IA-0001"),
    ;
    private final String value;
}
