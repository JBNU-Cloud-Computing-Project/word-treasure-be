package cloudcomputing.wordtreasure.api.game;

import cloudcomputing.wordtreasure.api.game.response.LiveRankingResponse;
import cloudcomputing.wordtreasure.api.game.response.RankingSuccessCode;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.LiveRankingEntry;
import cloudcomputing.wordtreasure.model.game.dto.MyRankingInfo;
import cloudcomputing.wordtreasure.model.game.service.LiveRankingService;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/game/rankings")
@RequiredArgsConstructor
@Tag(name = "순위", description = "실시간 순위 관련 API")
public class RankingController {

    private final LiveRankingService liveRankingService;

    /**
     * 실시간 순위 조회
     */
    @LoginRequired
    @GetMapping("/live")
    @Operation(
            summary = "실시간 순위 조회",
            description = "오늘의 게임 실시간 순위를 조회합니다. 상위 N명과 내 순위를 함께 반환합니다."
    )
    public ResponseEntity<ApiResponse<LiveRankingResponse>> getLiveRankings(
            @Login Long memberId,
            @RequestParam Long dailyWordId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        log.info("실시간 순위 조회 - memberId: {}, dailyWordId: {}, limit: {}",
                memberId, dailyWordId, limit);

        // 1. 상위 순위 조회
        List<LiveRankingEntry> rankings =
                liveRankingService.getLiveRankings(dailyWordId, limit);

        // 2. 내 순위 조회
        Optional<MyRankingInfo> myRank =
                liveRankingService.getMyRanking(dailyWordId, memberId);

        // 3. 전체 참여자 수
        long totalParticipants = liveRankingService.getTotalParticipants(dailyWordId);

        // 4. 응답 구성
        LiveRankingResponse response = LiveRankingResponse.of(
                rankings,
                myRank.orElse(null),
                totalParticipants
        );

        return ResponseEntity.ok(
                ApiResponse.success(RankingSuccessCode.LIVE_RANKING_INFO, response)
        );
    }
}
