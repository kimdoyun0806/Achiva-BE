package unicon.Achiva.domain.goal.dto;

import lombok.Builder;
import unicon.Achiva.domain.goal.entity.Goal;
import unicon.Achiva.domain.goal.entity.GoalCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record GoalResponse(
        UUID id,
        GoalCategory category,
        String text,
        Integer clickCount,
        Boolean isArchived,
        UUID memberId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static GoalResponse fromEntity(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .category(goal.getCategory())
                .text(goal.getText())
                .clickCount(goal.getClickCount())
                .isArchived(goal.getIsArchived())
                .memberId(goal.getMember().getId())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
