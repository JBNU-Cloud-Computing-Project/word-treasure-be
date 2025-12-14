package cloudcomputing.wordtreasure.api.game;

import cloudcomputing.wordtreasure.api.game.request.GameStartRequest;
import cloudcomputing.wordtreasure.api.game.request.HintRequest;
import cloudcomputing.wordtreasure.api.game.request.SubmitAttemptRequest;
import cloudcomputing.wordtreasure.api.game.response.*;
import cloudcomputing.wordtreasure.common.annotation.Login;
import cloudcomputing.wordtreasure.common.annotation.LoginRequired;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.game.dto.*;
import cloudcomputing.wordtreasure.model.game.service.GameDashboardService;
import cloudcomputing.wordtreasure.model.game.service.GamePlayService;
import cloudcomputing.wordtreasure.model.game.service.GameResultService;
import cloudcomputing.wordtreasure.model.game.service.GameSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Tag(name = "게임", description = "게임 플레이 관련 API")
public class GameController {

    private final GameDashboardService dashboardService;
    private final GamePlayService gamePlayService;
    private final GameSessionService gameSessionService;
    private final GameResultService gameResultService;

    /**
     * 현재 게임 상태 조회
     */
    @LoginRequired
    @GetMapping("/current")
    @Operation(summary = "현재 게임 상태 조회", description = "오늘의 단어와 사용자의 게임 진행 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<CurrentGameResponse>> getCurrentGame(
            @Login Long memberId
    ) {
        log.info("현재 게임 상태 조회 요청 - memberId: {}", memberId);

        CurrentGameInfo gameInfo = dashboardService.getCurrentGameInfo(memberId);
        CurrentGameResponse response = CurrentGameResponse.from(gameInfo);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.CURRENT_GAME_INFO, response)
        );
    }

    /**
     * 게임 시작
     */
    @LoginRequired
    @PostMapping("/start")
    @Operation(summary = "게임 시작", description = "오늘의 단어로 새로운 게임을 시작합니다.")
    public ResponseEntity<ApiResponse<GameStartResponse>> startGame(
            @Login Long memberId,
            @Valid @RequestBody GameStartRequest request
    ) {
        log.info("게임 시작 요청 - memberId: {}, dailyWordId: {}", memberId, request.dailyWordId());

        GameStartResult result = gamePlayService.startGame(memberId, request.dailyWordId());
        GameStartResponse response = GameStartResponse.from(result);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.GAME_STARTED, response)
        );
    }

    /**
     * 단어 시도 제출
     */
    @LoginRequired
    @PostMapping("/attempt")
    @Operation(summary = "단어 시도 제출", description = "사용자가 입력한 단어/문장을 제출하고 유사도를 확인합니다.")
    public ResponseEntity<ApiResponse<AttemptResponse>> submitAttempt(
            @Login Long memberId,
            @Valid @RequestBody SubmitAttemptRequest request
    ) {
        log.info("시도 제출 요청 - memberId: {}, gameSessionId: {}, userInput: {}",
                memberId, request.gameSessionId(), request.userInput());

        AttemptResult result = gamePlayService.submitAttempt(
                request.gameSessionId(),
                request.userInput()
        );

        AttemptResponse response = AttemptResponse.from(result);

        // 정답이면 다른 성공 메시지
        GameSuccessCode successCode = GameSuccessCode.ATTEMPT_SUBMITTED;

        return ResponseEntity.ok(
                ApiResponse.success(successCode, response)
        );
    }

    /**
     * 게임 세션 정보 조회
     */
    @LoginRequired
    @GetMapping("/session/{gameSessionId}")
    @Operation(summary = "게임 세션 정보 조회", description = "게임 세션의 상세 정보(시도 목록, 힌트 등)를 조회합니다.")
    public ResponseEntity<ApiResponse<GameSessionResponse>> getGameSession(
            @Login Long memberId,
            @PathVariable Long gameSessionId
    ) {
        log.info("게임 세션 조회 요청 - memberId: {}, gameSessionId: {}", memberId, gameSessionId);

        GameSessionDetail detail = gameSessionService.getGameSessionDetail(
                gameSessionId,
                memberId
        );

        GameSessionResponse response = GameSessionResponse.from(detail);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.SESSION_INFO, response)
        );
    }

    /**
     * 추가 힌트 요청
     */
    @LoginRequired
    @PostMapping("/hint")
    @Operation(summary = "추가 힌트 요청", description = "토큰을 소비하고 추가 힌트를 받습니다.")
    public ResponseEntity<ApiResponse<HintResponse>> requestHint(
            @Login Long memberId,
            @Valid @RequestBody HintRequest request
    ) {
        log.info("힌트 요청 - memberId: {}, gameSessionId: {}", memberId, request.gameSessionId());

        HintResult result = gamePlayService.requestHint(request.gameSessionId());
        HintResponse response = HintResponse.from(result);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.HINT_PROVIDED, response)
        );
    }

    /**
     * 게임 결과 조회
     */
    @LoginRequired
    @GetMapping("/result/{gameSessionId}")
    @Operation(
            summary = "게임 결과 조회",
            description = "완료된 게임의 최종 결과를 조회합니다. 성공/실패 여부에 따라 다른 정보를 제공합니다."
    )
    public ResponseEntity<ApiResponse<GameResultResponse>> getGameResult(
            @Login Long memberId,
            @PathVariable Long gameSessionId
    ) {
        log.info("게임 결과 조회 요청 - memberId: {}, gameSessionId: {}", memberId, gameSessionId);

        GameResult result = gameResultService.getGameResult(
                gameSessionId,
                memberId
        );

        GameResultResponse response = GameResultResponse.from(result);

        return ResponseEntity.ok(
                ApiResponse.success(GameSuccessCode.GAME_RESULT_INFO, response)
        );
    }
}
