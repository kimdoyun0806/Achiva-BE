package unicon.Achiva.domain.moim.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.moim.entity.Moim;
import unicon.Achiva.domain.moim.entity.MoimMember;
import unicon.Achiva.domain.moim.entity.MoimRole;

import java.util.List;

@Getter
@Builder
public class MoimResponse {
    private Long id;
    private String name;
    private String description;
    private List<Category> categories;
    private String leaderName;
    private int memberCount;
    private int maxMember;
    private int score;
    @com.fasterxml.jackson.annotation.JsonProperty("isPrivate")
    private boolean isPrivate;
    @com.fasterxml.jackson.annotation.JsonProperty("isOfficial")
    private boolean isOfficial;

    public static MoimResponse from(Moim moim) {
        String leader = moim.getMembers().stream()
                .filter(mm -> mm.getRole() == MoimRole.LEADER)
                .findFirst()
                .map(mm -> mm.getMember().getNickName())
                .orElse("방장없음");

        return MoimResponse.builder()
                .id(moim.getId())
                .name(moim.getName())
                .description(moim.getDescription())
                .categories(moim.getCategories())
                .leaderName(leader)
                .memberCount(moim.getMemberCount())
                .maxMember(moim.getMaxMember())
                .score(moim.getScore())
                .isPrivate(moim.isPrivate())
                .isOfficial(moim.isOfficial())
                .build();
    }
}
