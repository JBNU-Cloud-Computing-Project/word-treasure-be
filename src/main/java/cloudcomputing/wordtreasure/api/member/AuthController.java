package cloudcomputing.wordtreasure.api.member;

import cloudcomputing.wordtreasure.api.member.request.LoginRequest;
import cloudcomputing.wordtreasure.api.member.request.SignupRequest;
import cloudcomputing.wordtreasure.api.member.response.AuthSuccessCode;
import cloudcomputing.wordtreasure.api.member.response.CheckDuplicateResponse;
import cloudcomputing.wordtreasure.api.member.response.MemberResponse;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.config.SessionConst;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원가입/로그인 및 인증 관련 API")
public class AuthController {

    private final MemberService memberService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일/비밀번호/닉네임으로 회원가입을 수행합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        Member member = memberService.signup(request);
        MemberResponse response = MemberResponse.from(member);

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.SIGNUP_SUCCESS, response)
        );
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "세션 기반 로그인을 수행합니다. 성공 시 서버 세션이 생성됩니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        Member member = memberService.login(request);

        // 세션 생성
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, member.getMemberId());
        session.setMaxInactiveInterval(SessionConst.SESSION_TIMEOUT);

        log.info("세션 생성 - sessionId: {}, memberId: {}", session.getId(), member.getMemberId());

        MemberResponse response = MemberResponse.from(member);

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.LOGIN_SUCCESS, response)
        );
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인된 세션을 무효화합니다.")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Long memberId = (Long) session.getAttribute(SessionConst.LOGIN_MEMBER);
            log.info("로그아웃 - sessionId: {}, memberId: {}", session.getId(), memberId);
            session.invalidate();
        }

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.LOGOUT_SUCCESS)
        );
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check/email")
    @Operation(summary = "이메일 중복 확인", description = "이미 가입된 이메일인지 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<CheckDuplicateResponse>> checkEmailDuplicate(
            @RequestParam String email
    ) {
        boolean isDuplicate = memberService.isEmailDuplicate(email);

        CheckDuplicateResponse response = isDuplicate
                ? CheckDuplicateResponse.duplicate("이메일")
                : CheckDuplicateResponse.available();

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.EMAIL_AVAILABLE, response)
        );
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/check/nickname")
    @Operation(summary = "닉네임 중복 확인", description = "이미 사용 중인 닉네임인지 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<CheckDuplicateResponse>> checkNicknameDuplicate(
            @RequestParam String nickName
    ) {
        boolean isDuplicate = memberService.isNicknameDuplicate(nickName);

        CheckDuplicateResponse response = isDuplicate
                ? CheckDuplicateResponse.duplicate("닉네임")
                : CheckDuplicateResponse.available();

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.NICKNAME_AVAILABLE, response)
        );
    }

    /**
     * 현재 로그인한 회원 정보 조회
     */
    @LoginRequired
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MemberResponse>> getCurrentMember(@Login Long memberId) {

        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        Member member = memberService.findById(memberId);
        MemberResponse response = MemberResponse.from(member);

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.LOGIN_SUCCESS, response)
        );
    }
}
