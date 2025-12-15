package cloudcomputing.wordtreasure.model.member.dto;

import java.time.LocalDate;

public record FastestSolveInfo(
        String time,
        LocalDate gameDate,
        String word,
        Integer attemptCount
) {
}
