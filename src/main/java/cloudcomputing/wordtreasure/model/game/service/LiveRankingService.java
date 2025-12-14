package cloudcomputing.wordtreasure.model.game.service;

import cloudcomputing.wordtreasure.model.game.dto.LiveRankingEntry;
import cloudcomputing.wordtreasure.model.game.dto.MyRankingInfo;
import cloudcomputing.wordtreasure.model.game.dto.RankingEntry;
import cloudcomputing.wordtreasure.model.game.entity.GameSession;
import cloudcomputing.wordtreasure.model.game.repository.GameSessionRepository;
import cloudcomputing.wordtreasure.model.member.entity.Member;
import cloudcomputing.wordtreasure.model.member.repositroy.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveRankingService {

    private final RankingService rankingService;
    private final MemberRepository memberRepository;
    private final GameSessionRepository gameSessionRepository;

    /**
     * 실시간 순위 조회 (회원 정보 포함)
     *
     * @param dailyWordId 일일 단어 ID
     * @param limit       조회할 인원 수
     * @return 상위 순위 목록
     */
    public List<LiveRankingEntry> getLiveRankings(Long dailyWordId, int limit) {
        log.info("실시간 순위 조회 - dailyWordId: {}, limit: {}", dailyWordId, limit);

        // 1. Redis에서 상위 순위 조회
        List<RankingEntry> rankingEntries =
                rankingService.getTopRankings(dailyWordId, limit);

        if (rankingEntries.isEmpty()) {
            return List.of();
        }

        // 2. 회원 ID 목록 추출
        List<Long> memberIds = rankingEntries.stream()
                .map(RankingEntry::memberId)
                .collect(Collectors.toList());

        // 3. 회원 정보 일괄 조회
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m));

        // 4. 게임 세션 정보 일괄 조회
        Map<Long, GameSession> sessionMap = gameSessionRepository
                .findByMemberIdsAndDailyWordId(memberIds, dailyWordId).stream()
                .collect(Collectors.toMap(s -> s.getMember().getMemberId(), s -> s));

        // 5. 순위 엔트리 구성
        return rankingEntries.stream()
                .map(entry -> {
                    Member member = memberMap.get(entry.memberId());
                    GameSession session = sessionMap.get(entry.memberId());

                    if (member == null || session == null) {
                        log.warn("회원 또는 세션 정보 없음 - memberId: {}", entry.memberId());
                        return null;
                    }

                    return new LiveRankingEntry(
                            entry.rank(),
                            member.getNickName(),
                            session.getAttemptCount(),
                            calculateCompletionTime(session)
                    );
                })
                .filter(entry -> entry != null)
                .collect(Collectors.toList());
    }

    /**
     * 내 순위 조회
     *
     * @param dailyWordId 일일 단어 ID
     * @param memberId    회원 ID
     * @return 내 순위 정보
     */
    public Optional<MyRankingInfo> getMyRanking(Long dailyWordId, Long memberId) {
        log.info("내 순위 조회 - dailyWordId: {}, memberId: {}", dailyWordId, memberId);

        // 1. 게임 세션 조회
        Optional<GameSession> sessionOpt = gameSessionRepository
                .findByMemberIdAndDailyWordId(memberId, dailyWordId);

        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        GameSession session = sessionOpt.get();
        Member member = session.getMember();

        // 2. 게임 진행 중이면 순위 없음
        if (!session.getStatus().isSuccess()) {
            return Optional.of(new MyRankingInfo(
                    null,
                    member.getNickName(),
                    session.getStatus().getDescription()
            ));
        }

        // 3. Redis에서 순위 조회
        Integer rank = rankingService.getMemberRank(dailyWordId, memberId);

        return Optional.of(new MyRankingInfo(
                rank,
                member.getNickName(),
                session.getStatus().getDescription()
        ));
    }

    /**
     * 전체 참여자 수 조회
     *
     * @param dailyWordId 일일 단어 ID
     * @return 참여자 수
     */
    public long getTotalParticipants(Long dailyWordId) {
        return rankingService.getTotalParticipants(dailyWordId);
    }

    /**
     * 완료 시간 계산 (HH:MM:SS)
     */
    private String calculateCompletionTime(GameSession session) {
        if (session.getCompletedAt() == null || session.getStartedAt() == null) {
            return "진행중";
        }

        long seconds = Duration.between(
                session.getStartedAt(),
                session.getCompletedAt()
        ).getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
