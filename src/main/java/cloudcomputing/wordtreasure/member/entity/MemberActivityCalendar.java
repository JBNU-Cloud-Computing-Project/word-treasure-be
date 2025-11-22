package cloudcomputing.wordtreasure.member.entity;

import cloudcomputing.wordtreasure.game.entity.GameSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "member_activity_calendar",
        indexes = {
                @Index(name = "idx_activity_member", columnList = "member_id"),
                @Index(name = "idx_activity_date", columnList = "activity_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_activity_date",
                        columnNames = {"member_id", "activity_date"}
                )

        })
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberActivityCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationLevel participationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;
}
