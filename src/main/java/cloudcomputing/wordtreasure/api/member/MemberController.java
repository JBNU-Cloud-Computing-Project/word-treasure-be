package cloudcomputing.wordtreasure.api.member;

import cloudcomputing.wordtreasure.model.member.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 조회/수정 API (추가 예정)")
public class MemberController {

    private final AuthService authService;
}
