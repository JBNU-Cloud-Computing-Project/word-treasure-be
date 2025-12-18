package cloudcomputing.wordtreasure.common.config;

public final class SessionConst {
    public static final String LOGIN_MEMBER = "loginMember";
    public static final int SESSION_TIMEOUT = 86400;

    private SessionConst() {
        throw new AssertionError("상수 클래스는 인스턴스화할 수 없습니다.");
    }
}
