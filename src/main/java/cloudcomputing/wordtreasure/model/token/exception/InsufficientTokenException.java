package cloudcomputing.wordtreasure.model.token.exception;

public class InsufficientTokenException extends RuntimeException {
    public InsufficientTokenException(int currentTokens, int requiredTokens) {
        super(String.format("토큰 잔액이 부족합니다. (현재: %d, 필요: %d)",
                currentTokens, requiredTokens));
    }

    public InsufficientTokenException(String message) {
        super(message);
    }
}
