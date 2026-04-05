package unicon.Achiva.domain.moim.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.moim.entity.Moim;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class MoimDetailResponse {
    private Long id;
    private String name;
    private String description;
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

    private List<MoimMemberDto> members;

    public static MoimDetailResponse from(
            Moim moim,
            UUID currentUserId,
            Map<UUID, Long> scoreMap,
            Map<UUID, Long> postCountMap,
            Map<UUID, Long> weeklyStreakMap
    ) {
        List<MoimMemberDto> memberDtos = moim.getMembers().stream()
                .map(mm -> {
                    int score = scoreMap.getOrDefault(mm.getMember().getId(), 0L).intValue();
                    int posts = postCountMap.getOrDefault(mm.getMember().getId(), 0L).intValue();
                    int streak = weeklyStreakMap.getOrDefault(mm.getMember().getId(), 0L).intValue();
                    return MoimMemberDto.from(mm, currentUserId, score, posts, streak);
                })
                .collect(Collectors.toList());

        long sum = memberDtos.stream().mapToLong(MoimMemberDto::getMonthlyPosts).sum();

        return MoimDetailResponse.builder()
                .id(moim.getId())
                .name(moim.getName())
                .description(moim.getDescription())
                .memberCount(moim.getMemberCount())
                .maxMember(moim.getMaxMember())
                .score(moim.getScore())
                .isPrivate(moim.isPrivate())
                .isOfficial(moim.isOfficial())
                .groupGoalCurrent(sum)
                .groupGoalTarget((long) moim.getTargetAmount())
                .deadlineDaysLeft(ChronoUnit.DAYS.between(java.time.LocalDate.now(), java.time.LocalDate.now().plusMonths(1).withDayOfMonth(1)) - 1)
                .pokeDays(moim.getPokeDays())
                .members(memberDtos)
                .build();
    }
}
