package cloudcomputing.wordtreasure.common.config;

import cloudcomputing.wordtreasure.common.annotation.Login;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @Login 어노테이션이 있고, Long 타입인 파라미터만 지원
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        // 1. request attribute에서 먼저 확인 (인터셉터에서 저장한 값)
        Long memberId = (Long) request.getAttribute("memberId");

        if (memberId != null) {
            return memberId;
        }

        // 2. 세션에서 확인
        HttpSession session = request.getSession(false);
        if (session != null) {
            memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (memberId != null) {
                return memberId;
            }
        }

        // 3. 로그인 정보가 없으면 예외 발생
        log.error("로그인 정보를 찾을 수 없음 - URI: {}", request.getRequestURI());
        throw new IllegalStateException("로그인 정보를 찾을 수 없습니다.");
    }
}
