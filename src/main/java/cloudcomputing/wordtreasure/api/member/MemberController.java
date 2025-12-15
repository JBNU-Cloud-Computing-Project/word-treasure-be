package cloudcomputing.wordtreasure.api.member;

import cloudcomputing.wordtreasure.api.member.request.ChangePasswordRequest;
import cloudcomputing.wordtreasure.api.member.request.UpdateProfileRequest;
import cloudcomputing.wordtreasure.api.member.response.*;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.RecentGameInfo;
import cloudcomputing.wordtreasure.model.game.dto.UserStatisticsInfo;
import cloudcomputing.wordtreasure.model.game.service.GameDashboardService;
import cloudcomputing.wordtreasure.model.member.dto.ActivityInfo;
import cloudcomputing.wordtreasure.model.member.dto.BestRecords;
import cloudcomputing.wordtreasure.model.member.dto.ProfileInfo;
import cloudcomputing.wordtreasure.model.member.service.ActivityCalendarService;
import cloudcomputing.wordtreasure.model.member.service.BestRecordsService;
import cloudcomputing.wordtreasure.model.member.service.MemberProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "회원", description = "회원 관련 조회/수정")
public class MemberController {

    private final MemberProfileService memberProfileService;
    private final ActivityCalendarService activityCalendarService;
    private final BestRecordsService bestRecordsService;
    private final GameDashboardService dashboardService;

    /**
     * 프로필 정보 조회
     */
    @LoginRequired
    @GetMapping("/profile")
    @Operation(summary = "프로필 정보 조회", description = "회원의 기본 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @Login Long memberId
    ) {
        log.info("프로필 정보 조회 요청 - memberId: {}", memberId);

        ProfileInfo profileInfo = memberProfileService.getProfile(memberId);
        ProfileResponse response = ProfileResponse.from(profileInfo);

        return ResponseEntity.ok(
                ApiResponse.success(MemberSuccessCode.PROFILE_INFO, response)
        );
    }

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

    /**
     * 활동 캘린더 조회
     */
    @LoginRequired
    @GetMapping("/activity-calendar")
    @Operation(
            summary = "활동 캘린더 조회",
            description = "특정 기간의 게임 참여 기록을 조회합니다. 최근 30일 데이터를 활용합니다."
    )
    public ResponseEntity<ApiResponse<ActivityCalendarResponse>> getActivityCalendar(
            @Login Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("활동 캘린더 조회 요청 - memberId: {}, startDate: {}, endDate: {}",
                memberId, startDate, endDate);

        List<ActivityInfo> activities =
                activityCalendarService.getActivityCalendar(memberId, startDate, endDate);
        ActivityCalendarResponse response = ActivityCalendarResponse.from(activities);

        return ResponseEntity.ok(
                ApiResponse.success(MemberSuccessCode.ACTIVITY_CALENDAR_INFO, response)
        );
    }

    /**
     * 최고 기록 조회
     */
    @LoginRequired
    @GetMapping("/best-records")
    @Operation(summary = "최고 기록 조회", description = "회원의 최고 기록들을 조회합니다.")
    public ResponseEntity<ApiResponse<BestRecordsResponse>> getBestRecords(
            @Login Long memberId
    ) {
        log.info("최고 기록 조회 요청 - memberId: {}", memberId);

        BestRecords records = bestRecordsService.getBestRecords(memberId);
        BestRecordsResponse response = BestRecordsResponse.from(records);

        return ResponseEntity.ok(
                ApiResponse.success(MemberSuccessCode.BEST_RECORDS_INFO, response)
        );
    }

    /**
     * 프로필 수정
     */
    @LoginRequired
    @PatchMapping("/profile")
    @Operation(summary = "프로필 수정", description = "닉네임을 수정합니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
            @Login Long memberId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("프로필 수정 요청 - memberId: {}, newNickname: {}", memberId, request.nickname());

        ProfileInfo updatedProfile =
                memberProfileService.updateProfile(memberId, request.nickname());
        ProfileResponse response = ProfileResponse.from(updatedProfile);

        return ResponseEntity.ok(
                ApiResponse.success(MemberSuccessCode.PROFILE_UPDATED, response)
        );
    }

    /**
     * 비밀번호 변경
     */
    @LoginRequired
    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 확인하고 새 비밀번호로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Login Long memberId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("비밀번호 변경 요청 - memberId: {}", memberId);

        memberProfileService.changePassword(
                memberId,
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                ApiResponse.success(MemberSuccessCode.PASSWORD_CHANGED, null)
        );
    }
}
