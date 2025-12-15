package cloudcomputing.wordtreasure.model.member.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HighestFirstTryInfo(
        BigDecimal similarity,
        LocalDate gameDate,
        String word,
        String userInput
) {
}
