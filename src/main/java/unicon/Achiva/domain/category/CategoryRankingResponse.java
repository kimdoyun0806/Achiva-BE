package unicon.Achiva.domain.category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "카테고리별 랭킹 응답")
public record CategoryRankingResponse(
        @Schema(description = "카테고리별 랭킹 목록")
        List<CategoryRanking> categories
) {

    @Schema(description = "카테고리별 유저 랭킹")
    public record CategoryRanking(
            @Schema(description = "카테고리명", example = "헬스")
            String category,
            @Schema(description = "해당 카테고리 유저 목록")
            List<CategoryRankingMember> members
    ) {
    }

    @Schema(description = "카테고리 랭킹 유저 정보")
    public record CategoryRankingMember(
            @Schema(description = "회원 ID")
            UUID memberId,
            @Schema(description = "닉네임", example = "achiva_user")
            String nickName,
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
            String profileImageUrl,
            @Schema(description = "해당 카테고리 게시글 수", example = "12")
            Long articleCount
    ) {
    }
}
