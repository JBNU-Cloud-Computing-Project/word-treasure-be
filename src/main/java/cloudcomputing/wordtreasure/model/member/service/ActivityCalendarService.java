package cloudcomputing.wordtreasure.model.member.service;

import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.entity.GameStatus;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.dto.ActivityInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityCalendarService {

    private final GameSessionRepository gameSessionRepository;

    /**
     * 활동 캘린더 조회
     *
     * @param memberId  회원 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 활동 목록
     */
    public List<ActivityInfo> getActivityCalendar(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("활동 캘린더 조회 - memberId: {}, startDate: {}, endDate: {}",
                memberId, startDate, endDate);

        // 해당 기간의 게임 세션 조회
        List<GameSession> sessions = gameSessionRepository
                .findByMemberIdAndDateRange(memberId, startDate, endDate);

        // ActivityInfo로 변환
        return sessions.stream()
                .map(session -> {
                    LocalDate activityDate = session.getStartedAt().toLocalDate();
                    int participationLevel = calculateParticipationLevel(session);

                    return new ActivityInfo(
                            activityDate,
                            participationLevel,
                            session.getId(),
                            session.getStatus()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 참여 레벨 계산
     * <p>
     * participationLevel:
     * - 1: 게임 참여만 (최고 유사도 < 50%)
     * - 2: 50% 이상 유사도 달성
     * - 3: 80% 이상 유사도 달성
     * - 4: 정답 성공
     */
    private int calculateParticipationLevel(GameSession session) {
        if (session.getStatus() == GameStatus.SUCCESS) {
            return 4;  // 정답 성공
        }

        BigDecimal highestSimilarity = session.getHighestSimilarity();
        if (highestSimilarity == null) {
            return 1;  // 시도만 함
        }

        if (highestSimilarity.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return 3;  // 80% 이상
        } else if (highestSimilarity.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return 2;  // 50% 이상
        } else {
            return 1;  // 50% 미만
        }
    }
}
