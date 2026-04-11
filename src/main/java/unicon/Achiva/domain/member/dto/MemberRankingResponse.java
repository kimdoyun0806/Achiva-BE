package unicon.Achiva.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.member.entity.Member;

import java.util.Optional;

@Getter
@Builder
@Schema(description = "랭킹 기능용 회원 정보 응답")
public class MemberRankingResponse {

    @Schema(description = "닉네임", example = "achiva_user#1234")
    private String nickName;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    private String profileImageUrl;

    @Schema(description = "총 게시글 수", example = "12")
    private long articleCount;

    @Schema(description = "이번 주 작성한 게시글 수", example = "4")
    private int weeklyWorkoutCount;

    @Schema(description = "연속 목표 달성 주차", example = "3")
    private int continuousGoalWeeks;

    public static MemberRankingResponse from(
            Member member,
            long articleCount,
            MemberStatsResponse memberStatsResponse
    ) {
        return MemberRankingResponse.builder()
                .nickName(member.getNickName())
                .profileImageUrl(member.getProfileImageUrl())
                .articleCount(articleCount)
                .weeklyWorkoutCount(Optional.ofNullable(memberStatsResponse.getWeeklyWorkoutCount()).orElse(0))
                .continuousGoalWeeks(Optional.ofNullable(memberStatsResponse.getContinuousGoalWeeks()).orElse(0))
                .build();
    }
}
