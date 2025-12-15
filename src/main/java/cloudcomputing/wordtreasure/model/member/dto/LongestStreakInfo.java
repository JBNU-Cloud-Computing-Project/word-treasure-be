package cloudcomputing.wordtreasure.model.member.dto;

import java.time.LocalDate;

public record LongestStreakInfo(
        Integer streakDays,
        LocalDate startDate,
        LocalDate endDate
) {
}
