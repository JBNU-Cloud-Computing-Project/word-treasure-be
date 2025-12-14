package cloudcomputing.wordtreasure.api.member;

import cloudcomputing.wordtreasure.api.member.response.AuthSuccessCode;
import cloudcomputing.wordtreasure.api.member.response.RecentGameResponse;
import cloudcomputing.wordtreasure.api.member.response.UserStatisticsResponse;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.RecentGameInfo;
import cloudcomputing.wordtreasure.model.game.dto.UserStatisticsInfo;
import cloudcomputing.wordtreasure.model.game.service.GameDashboardService;
import cloudcomputing.wordtreasure.model.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 조회/수정 API (추가 예정)")
public class MemberController {

    private final AuthService authService;
    private final GameDashboardService dashboardService;

    /**
     * 사용자 통계 조회
     */
    @LoginRequired
    @GetMapping("/statistics")
    @Operation(summary = "사용자 통계 조회", description = "로그인한 회원의 게임 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics(
            @Login Long memberId
    ) {
        log.info("사용자 통계 조회 요청 - memberId: {}", memberId);

        UserStatisticsInfo statisticsInfo = dashboardService.getUserStatistics(memberId);
        UserStatisticsResponse response = UserStatisticsResponse.from(statisticsInfo);

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.MEMBER_INFO, response)
        );
    }

    /**
     * 최근 게임 기록 조회
     */
    @LoginRequired
    @GetMapping("/recent-games")
    @Operation(summary = "최근 게임 기록 조회", description = "로그인한 회원의 최근 게임 기록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RecentGameResponse>>> getRecentGames(
            @Login Long memberId,
            @RequestParam(defaultValue = "3") int limit
    ) {
        log.info("최근 게임 기록 조회 요청 - memberId: {}, limit: {}", memberId, limit);

        List<RecentGameInfo> recentGames = dashboardService.getRecentGames(memberId, limit);
        List<RecentGameResponse> response = recentGames.stream()
                .map(RecentGameResponse::from)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success(AuthSuccessCode.MEMBER_INFO, response)
        );
    }
}
