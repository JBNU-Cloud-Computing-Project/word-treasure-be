package cloudcomputing.wordtreasure.common.config;

import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. Handler가 메서드인지 확인
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 2. @LoginRequired 어노테이션 확인
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);

        // 어노테이션이 없으면 통과
        if (loginRequired == null) {
            return true;
        }

        // 3. 세션에서 로그인 정보 확인
        HttpSession session = request.getSession(false);

        if (session == null) {
            log.warn("로그인 필요 - 세션 없음: {} {}", request.getMethod(), request.getRequestURI());
            sendUnauthorizedResponse(response, "로그인이 필요합니다.");
            return false;
        }

        Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (memberId == null) {
            log.warn("로그인 필요 - 세션에 회원 정보 없음: {} {}",
                    request.getMethod(), request.getRequestURI());
            sendUnauthorizedResponse(response, "로그인이 필요합니다.");
            return false;
        }

        // 4. 로그인 정보를 request attribute에 저장 (Controller에서 사용 가능)
        request.setAttribute("memberId", memberId);

        log.debug("로그인 검증 통과 - memberId: {}, uri: {}", memberId, request.getRequestURI());

        return true;
    }

    //404
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"code\":\"AU-E001\",\"message\":\"%s\"}",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}
