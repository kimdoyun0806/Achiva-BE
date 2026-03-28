package unicon.Achiva.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatsResponse {
    private Integer weeklyWorkoutCount;
    private Integer continuousGoalWeeks;
}
