package cloudcomputing.wordtreasure.api.member.response;

public record CheckDuplicateResponse(
        boolean isDuplicate,
        String message
) {
    public static CheckDuplicateResponse available() {
        return new CheckDuplicateResponse(false, "사용 가능합니다.");
    }

    public static CheckDuplicateResponse duplicate(String fieldName) {
        return new CheckDuplicateResponse(true, fieldName + "이(가) 이미 사용 중입니다.");
    }
}
