package cloudcomputing.wordtreasure.model.member.dto;

import java.time.LocalDate;

public record BestRankInfo(
        Integer rank,
        LocalDate gameDate,
        String word
) {
}
