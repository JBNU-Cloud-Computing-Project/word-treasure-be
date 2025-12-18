package cloudcomputing.wordtreasure.api.token;

import cloudcomputing.wordtreasure.api.token.response.TokenSuccessCode;
import cloudcomputing.wordtreasure.common.controller.response.ApiResponse;
import cloudcomputing.wordtreasure.model.token.entity.DailyTokenPool;
import cloudcomputing.wordtreasure.model.token.service.TokenPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/token-pool")
@RequiredArgsConstructor
@Tag(name = "Token Pool", description = "토큰 풀 API")
public class TokenPoolController {
    private final TokenPoolService tokenPoolService;

    /**
     * 오늘의 토큰 풀 조회
     * - 메인 화면에 표시할 현재 모인 토큰 풀 정보
     */
    @GetMapping("/today")
    @Operation(summary = "오늘의 토큰 풀 조회",
            description = "현재까지 모인 오늘의 토큰 풀 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TodayTokenPoolResponse>> getTodayTokenPool() {
        log.info("오늘의 토큰 풀 조회 요청");

        LocalDate today = LocalDate.now();
        DailyTokenPool pool = tokenPoolService.getPoolByDate(today);

        TodayTokenPoolResponse response;

        if (pool == null) {
            // 아직 풀이 생성되지 않은 경우 (오늘 아무도 플레이 안 함)
            response = new TodayTokenPoolResponse(
                    today,
                    0L,
                    0L,
                    0L,
                    false
            );
        } else {
            response = new TodayTokenPoolResponse(
                    pool.getGameDate(),
                    pool.getTotalPool(),
                    pool.getCarryOver(),
                    pool.getDailyAccumulated(),
                    pool.getIsDistributed()
            );
        }

        log.info("오늘의 토큰 풀 조회 완료 - totalPool: {}", response.totalPool());

        return ResponseEntity.ok(
                ApiResponse.success(
                        TokenSuccessCode.TOKEN_SUCCESS_CODE,
                        response
                )
        );
    }

    /**
     * 오늘의 토큰 풀 응답 DTO
     */
    public record TodayTokenPoolResponse(
            LocalDate gameDate,          // 게임 날짜
            Long totalPool,              // 총 풀 금액 (이월 + 당일 적립)
            Long carryOver,              // 전날 이월분
            Long dailyAccumulated,       // 오늘 적립된 금액
            Boolean isDistributed        // 배분 완료 여부
    ) {
    }
}
