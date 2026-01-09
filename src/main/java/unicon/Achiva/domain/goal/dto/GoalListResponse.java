package unicon.Achiva.domain.goal.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
public record GoalListResponse(
        List<GoalResponse> goals,
        Integer total
) {
    public static GoalListResponse fromGoals(List<GoalResponse> goals) {
        return GoalListResponse.builder()
                .goals(goals)
                .total(goals.size())
                .build();
    }

    public static GoalListResponse groupedByCategory(List<GoalResponse> goals) {
        return GoalListResponse.builder()
                .goals(goals)
                .total(goals.size())
                .build();
    }
}
