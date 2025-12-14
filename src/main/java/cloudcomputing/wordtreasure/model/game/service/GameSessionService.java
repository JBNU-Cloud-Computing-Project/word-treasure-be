package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.AttemptInfo;
import cloudcomputing.wordtreasure.model.game.dto.ExtraHintInfo;
import cloudcomputing.wordtreasure.model.game.dto.GameSessionDetail;
import cloudcomputing.wordtreasure.model.game.entity.Attempt;
import cloudcomputing.wordtreasure.model.game.entity.ExtraHint;
import cloudcomputing.wordtreasure.model.game.entity.GameConfigKey;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.repository.AttemptRepository;
import cloudcomputing.wordtreasure.model.game.repository.ExtraHintRepository;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final AttemptRepository attemptRepository;
    private final ExtraHintRepository extraHintRepository;
    private final GameConfigService gameConfigService;

    /**
     * 게임 세션 상세 정보 조회
     */
    public GameSessionDetail getGameSessionDetail(Long gameSessionId, Long memberId) {
        log.info("게임 세션 조회 - gameSessionId: {}, memberId: {}", gameSessionId, memberId);

        // 1. 게임 세션 조회
        GameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 세션입니다."));

        // 2. 본인의 게임 세션인지 확인
        if (!session.getMember().getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 게임 세션이 아닙니다.");
        }

        // 3. 시도 목록 조회
        List<Attempt> attempts = attemptRepository.findByGameSessionIdOrderByAttemptNumber(gameSessionId);

        // 4. 추가 힌트 목록 조회
        List<ExtraHint> extraHints = extraHintRepository.findByGameSessionIdOrderByCreatedAt(gameSessionId);

        // 5. 남은 시도 횟수 계산
        int maxAttempts = gameConfigService.getIntValue(GameConfigKey.MAX_ATTEMPTS);
        int remainingAttempts = maxAttempts - session.getAttemptCount();

        // 6. 현재 토큰 조회
        int currentTokens = session.getMember().getCurrentTokens();

        log.info("게임 세션 조회 완료 - attemptCount: {}, extraHintCount: {}",
                attempts.size(), extraHints.size());

        return new GameSessionDetail(
                session.getId(),
                session.getStatus(),
                session.getAttemptCount(),
                session.getHighestSimilarity(),
                remainingAttempts,
                currentTokens,
                attempts.stream().map(AttemptInfo::from).toList(),
                extraHints.stream().map(ExtraHintInfo::from).toList()
        );
    }


}
