package unicon.Achiva.domain.moim.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.moim.entity.Moim;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
public class MoimRankingResponse {
    private Long id;
    private String name;
    private String description;
    private List<Category> categories;
    private int memberCount;
    private int maxMember;
    private int score;
    @com.fasterxml.jackson.annotation.JsonProperty("isPrivate")
    private boolean isPrivate;
    @com.fasterxml.jackson.annotation.JsonProperty("isOfficial")
    private boolean isOfficial;
    private Long groupGoalCurrent;
    private Long groupGoalTarget;
    private Long deadlineDaysLeft;
    private int pokeDays;

    public static MoimRankingResponse from(Moim moim, long groupGoalCurrent) {
        return MoimRankingResponse.builder()
                .id(moim.getId())
                .name(moim.getName())
                .description(moim.getDescription())
                .categories(moim.getCategories())
                .memberCount(moim.getMemberCount())
                .maxMember(moim.getMaxMember())
                .score(moim.getScore())
                .isPrivate(moim.isPrivate())
                .isOfficial(moim.isOfficial())
                .groupGoalCurrent(groupGoalCurrent)
                .groupGoalTarget((long) moim.getTargetAmount())
                .deadlineDaysLeft(ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        LocalDate.now().plusMonths(1).withDayOfMonth(1)
                ) - 1)
                .pokeDays(moim.getPokeDays())
                .build();
    }
}
