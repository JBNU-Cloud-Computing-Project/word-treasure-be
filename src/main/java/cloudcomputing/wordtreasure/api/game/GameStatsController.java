package cloudcomputing.wordtreasure.api.game;

import cloudcomputing.wordtreasure.api.game.response.DifficultyStatsResponse;
import cloudcomputing.wordtreasure.api.game.response.GameSuccessCode;
import cloudcomputing.wordtreasure.api.game.response.TodayWordStatsResponse;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.AllDifficultyStats;
import cloudcomputing.wordtreasure.model.game.dto.TodayWordStats;
import cloudcomputing.wordtreasure.model.game.service.GameStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/game/stats")
@RequiredArgsConstructor
@Tag(name = "게임 통계", description = "오늘의 단어 및 난이도별 통계 API")
public class GameStatsController {

    private final GameStatsService gameStatsService;

    /**
     * 오늘의 단어 통계 조회
     */
    @LoginRequired
    @GetMapping("/today")
    @Operation(
            summary = "오늘의 단어 통계 조회",
            description = "오늘의 단어, 난이도, 참여자/성공자 수, 평균 시도 횟수를 조회합니다."
    )
    public ResponseEntity<ApiResponse<TodayWordStatsResponse>> getTodayWordStats() {
        log.info("오늘의 단어 통계 조회 요청");

        TodayWordStats stats = gameStatsService.getTodayWordStats();
        TodayWordStatsResponse response = TodayWordStatsResponse.from(stats);

        if (response == null) {
            return ResponseEntity.ok(
                    ApiResponse.success(GameSuccessCode.CURRENT_GAME_INFO, null)
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.CURRENT_GAME_INFO, response)
        );
    }

    /**
     * 난이도별 통계 조회
     */
    @LoginRequired
    @GetMapping("/difficulty")
    @Operation(
            summary = "난이도별 통계 조회",
            description = "쉬움/중급/어려움 각 난이도별 성공률과 게임 수를 조회합니다."
    )
    public ResponseEntity<ApiResponse<DifficultyStatsResponse>> getDifficultyStats() {
        log.info("난이도별 통계 조회 요청");

        AllDifficultyStats stats = gameStatsService.getAllDifficultyStats();
        DifficultyStatsResponse response = DifficultyStatsResponse.from(stats);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.CURRENT_GAME_INFO, response)
        );
    }
}
