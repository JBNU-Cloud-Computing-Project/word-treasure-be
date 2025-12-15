package cloudcomputing.wordtreasure.model.game.dto;

import java.time.LocalDate;

public record LeaderboardPeriod(
        String startDate,
        String endDate,
        Integer year,
        Integer month
) {
    public static LeaderboardPeriod ofDaily(LocalDate date) {
        return new LeaderboardPeriod(
                date.toString(),
                date.toString(),
                null,
                null
        );
    }

    public static LeaderboardPeriod ofWeekly(LocalDate startDate, LocalDate endDate) {
        return new LeaderboardPeriod(
                startDate.toString(),
                endDate.toString(),
                null,
                null
        );
    }

    public static LeaderboardPeriod ofMonthly(int year, int month) {
        return new LeaderboardPeriod(
                null,
                null,
                year,
                month
        );
    }

    public static LeaderboardPeriod ofAllTime() {
        return new LeaderboardPeriod(null, null, null, null);
    }
}
