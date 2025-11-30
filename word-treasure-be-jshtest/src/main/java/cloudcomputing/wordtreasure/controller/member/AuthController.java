package cloudcomputing.wordtreasure.controller.member;

import cloudcomputing.wordtreasure.application.member.AuthService;
import cloudcomputing.wordtreasure.controller.member.dto.LoginRequest;
import cloudcomputing.wordtreasure.controller.member.dto.SignupRequest;
import cloudcomputing.wordtreasure.domain.member.Members;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        Members saved = authService.signup(req);
        return ResponseEntity.ok("회원가입 완료. memberId = " + saved.getMemberId());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Members member = authService.login(req);
        return ResponseEntity.ok("로그인 성공. memberId = " + member.getMemberId());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok("로그아웃 완료");
    }
}
