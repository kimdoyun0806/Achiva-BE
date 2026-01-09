package unicon.Achiva.domain.goal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import unicon.Achiva.domain.goal.entity.GoalCategory;

public record GoalRequest(
        @NotNull(message = "카테고리는 필수입니다.")
        GoalCategory category,

        @NotNull(message = "목표 내용은 필수입니다.")
        @Size(min = 1, max = 200, message = "목표 내용은 1자 이상 200자 이하여야 합니다.")
        String text
) {
}
