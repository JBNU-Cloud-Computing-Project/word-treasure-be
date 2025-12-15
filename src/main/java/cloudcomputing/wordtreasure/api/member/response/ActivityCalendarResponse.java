package cloudcomputing.wordtreasure.api.member.response;

import cloudcomputing.wordtreasure.model.member.dto.ActivityInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record ActivityCalendarResponse(
        List<ActivityItemResponse> activities
) {
    public static ActivityCalendarResponse from(List<ActivityInfo> activities) {
        List<ActivityItemResponse> items = activities.stream()
                .map(ActivityItemResponse::from)
                .collect(Collectors.toList());

        return new ActivityCalendarResponse(items);
    }

    /**
     * 활동 아이템
     */
    record ActivityItemResponse(
            LocalDate activityDate,
            Integer participationLevel,
            Long gameSessionId,
            String status
    ) {
        static ActivityItemResponse from(ActivityInfo info) {
            return new ActivityItemResponse(
                    info.activityDate(),
                    info.participationLevel(),
                    info.gameSessionId(),
                    info.status().name()
            );
        }
    }
}
