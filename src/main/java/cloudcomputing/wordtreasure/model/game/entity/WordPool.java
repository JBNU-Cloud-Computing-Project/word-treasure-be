package cloudcomputing.wordtreasure.model.game.entity;

import cloudcomputing.wordtreasure.common.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "word_pool",
        indexes = {
                @Index(name = "idx_word_pool_difficulty", columnList = "difficulty"),
                @Index(name = "idx_word_pool_category", columnList = "category"),
                @Index(name = "idx_word_pool_active", columnList = "is_active"),
                @Index(name = "idx_word_pool_last_used", columnList = "last_used_date")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordPool extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String exampleSentence;

    /**
     * 유의어 리스트 (JSON 형태로 저장)
     * 예: ["배신자", "반역자", "변절자"]
     */
    @Column(columnDefinition = "TEXT")
    private String synonyms;

    /**
     * 이 단어가 출제된 횟수
     */
    @Column(nullable = false)
    private Integer usageCount = 0;

    /**
     * 마지막으로 출제된 날짜
     */
    private LocalDate lastUsedDate;

    /**
     * 활성화 여부 (비활성화된 단어는 출제되지 않음)
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder
    public WordPool(String word, String description, Difficulty difficulty,
                    String category, String exampleSentence, String synonyms) {
        this.word = word;
        this.description = description;
        this.difficulty = difficulty;
        this.category = category;
        this.exampleSentence = exampleSentence;
        this.synonyms = synonyms;
        this.usageCount = 0;
        this.isActive = true;
    }

    /**
     * 단어가 출제될 때 호출 - 사용 통계 업데이트
     */
    public void markAsUsed(LocalDate usedDate) {
        this.usageCount++;
        this.lastUsedDate = usedDate;
    }

    /**
     * 단어 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 단어 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 단어 정보 업데이트
     */
    public void updateInfo(String description, String exampleSentence, String synonyms) {
        if (description != null) {
            this.description = description;
        }
        if (exampleSentence != null) {
            this.exampleSentence = exampleSentence;
        }
        if (synonyms != null) {
            this.synonyms = synonyms;
        }
    }
}
