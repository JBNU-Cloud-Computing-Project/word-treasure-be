package cloudcomputing.wordtreasure.common.config;

public final class SessionConst {
    public static final String LOGIN_MEMBER = "loginMember";
    public static final int SESSION_TIMEOUT = 3600; // 1시간 (초 단위)

    private SessionConst() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
