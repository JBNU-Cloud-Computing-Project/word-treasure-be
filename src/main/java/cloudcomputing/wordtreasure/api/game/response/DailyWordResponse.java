package cloudcomputing.wordtreasure.api.game.response;

import cloudcomputing.wordtreasure.model.game.dto.DailyWordInfo;

public record DailyWordResponse(
        String word,
        String description
) {
    public static DailyWordResponse from(DailyWordInfo info) {
        return new DailyWordResponse(
                info.word(),
                info.description()
        );
    }
}
