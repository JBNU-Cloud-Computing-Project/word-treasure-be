package cloudcomputing.wordtreasure.api.game;

import cloudcomputing.wordtreasure.api.game.response.LeaderboardResponse;
import cloudcomputing.wordtreasure.api.game.response.RankingSuccessCode;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.LeaderboardEntry;
import cloudcomputing.wordtreasure.model.game.dto.LeaderboardPeriod;
import cloudcomputing.wordtreasure.model.game.dto.MyLeaderboardRanking;
import cloudcomputing.wordtreasure.model.game.dto.PaginationInfo;
import cloudcomputing.wordtreasure.model.game.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
@Tag(name = "리더보드", description = "기간별 순위표 조회 API")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * 일간 순위 조회
     */
    @LoginRequired
    @GetMapping("/daily")
    @Operation(
            summary = "일간 순위 조회",
            description = "특정 날짜의 리더보드를 조회합니다. 페이지네이션을 지원하며 내 순위도 함께 반환합니다."
    )
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getDailyRanking(
            @Login Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("일간 순위 조회 - memberId: {}, date: {}, page: {}, size: {}",
                memberId, targetDate, page, size);

        // 1. 순위 목록 조회
        List<LeaderboardEntry> rankings = leaderboardService.getDailyLeaderboard(targetDate, page, size);

        // 2. 내 순위 조회
        MyLeaderboardRanking myRanking = leaderboardService.getMyDailyRanking(targetDate, memberId);

        // 3. 전체 참여자 수
        long totalParticipants = leaderboardService.getTotalParticipants(targetDate, targetDate);

        // 4. 페이지네이션 정보
        PaginationInfo pagination = PaginationInfo.of(page, size, totalParticipants);

        // 5. 응답 구성
        LeaderboardResponse response = LeaderboardResponse.of(
                LeaderboardPeriod.ofDaily(targetDate),
                rankings,
                myRanking,
                pagination
        );

        return ResponseEntity.ok(
                ApiResponse.success(RankingSuccessCode.DAILY_RANKING_INFO, response)
        );
    }

    /**
     * 주간 순위 조회
     */
    @LoginRequired
    @GetMapping("/weekly")
    @Operation(
            summary = "주간 순위 조회",
            description = "주간 리더보드를 조회합니다. startDate와 endDate로 기간을 지정할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getWeeklyRanking(
            @Login Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("주간 순위 조회 - memberId: {}, start: {}, end: {}, page: {}, size: {}",
                memberId, startDate, endDate, page, size);

        // 1. 순위 목록 조회
        List<LeaderboardEntry> rankings = leaderboardService.getWeeklyLeaderboard(startDate, endDate, page, size);

        // 2. 내 순위 조회
        MyLeaderboardRanking myRanking = leaderboardService.getMyWeeklyRanking(startDate, endDate, memberId);

        // 3. 전체 참여자 수
        long totalParticipants = leaderboardService.getTotalParticipants(startDate, endDate);

        // 4. 페이지네이션 정보
        PaginationInfo pagination = PaginationInfo.of(page, size, totalParticipants);

        // 5. 응답 구성
        LeaderboardResponse response = LeaderboardResponse.of(
                LeaderboardPeriod.ofWeekly(startDate, endDate),
                rankings,
                myRanking,
                pagination
        );

        return ResponseEntity.ok(
                ApiResponse.success(RankingSuccessCode.WEEKLY_RANKING_INFO, response)
        );
    }

    /**
     * 월간 순위 조회
     */
    @LoginRequired
    @GetMapping("/monthly")
    @Operation(
            summary = "월간 순위 조회",
            description = "월간 리더보드를 조회합니다. year와 month로 특정 월을 지정합니다."
    )
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getMonthlyRanking(
            @Login Long memberId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("월간 순위 조회 - memberId: {}, year: {}, month: {}, page: {}, size: {}",
                memberId, year, month, page, size);

        // 1. 순위 목록 조회
        List<LeaderboardEntry> rankings = leaderboardService.getMonthlyLeaderboard(year, month, page, size);

        // 2. 내 순위 조회
        MyLeaderboardRanking myRanking = leaderboardService.getMyMonthlyRanking(year, month, memberId);

        // 3. 월의 시작/종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 4. 전체 참여자 수
        long totalParticipants = leaderboardService.getTotalParticipants(startDate, endDate);

        // 5. 페이지네이션 정보
        PaginationInfo pagination = PaginationInfo.of(page, size, totalParticipants);

        // 6. 응답 구성
        LeaderboardResponse response = LeaderboardResponse.of(
                LeaderboardPeriod.ofMonthly(year, month),
                rankings,
                myRanking,
                pagination
        );

        return ResponseEntity.ok(
                ApiResponse.success(RankingSuccessCode.MONTHLY_RANKING_INFO, response)
        );
    }

    /**
     * 전체 기간 순위 조회
     */
    @LoginRequired
    @GetMapping("/all-time")
    @Operation(
            summary = "전체 기간 순위 조회",
            description = "전체 기간 누적 리더보드를 조회합니다."
    )
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getAllTimeRanking(
            @Login Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("전체 순위 조회 - memberId: {}, page: {}, size: {}", memberId, page, size);

        // 1. 순위 목록 조회
        List<LeaderboardEntry> rankings = leaderboardService.getAllTimeLeaderboard(page, size);

        // 2. 내 순위 조회
        MyLeaderboardRanking myRanking = leaderboardService.getMyAllTimeRanking(memberId);

        // 3. 전체 참여자 수 (전체 기간)
        long totalParticipants = leaderboardService.getTotalParticipants(null, null);

        // 4. 페이지네이션 정보
        PaginationInfo pagination = PaginationInfo.of(page, size, totalParticipants);

        // 5. 응답 구성
        LeaderboardResponse response = LeaderboardResponse.of(
                LeaderboardPeriod.ofAllTime(),
                rankings,
                myRanking,
                pagination
        );

        return ResponseEntity.ok(
                ApiResponse.success(RankingSuccessCode.ALL_TIME_RANKING_INFO, response)
        );
    }
}
