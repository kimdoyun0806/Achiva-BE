package unicon.Achiva.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.auth.Role;
import unicon.Achiva.domain.member.Gender;
import unicon.Achiva.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "회원 정보 응답")
public class MemberResponse {
    @Schema(description = "회원 ID")
    private UUID id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "achiva_user")
    private String nickName;

    @Schema(description = "생년월일", example = "2000-01-01")
    private LocalDate birth;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "지역", example = "서울")
    private String region;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.png")
    private String profileImageUrl;

    @Schema(description = "자기소개", example = "꾸준히 운동 기록을 남기고 있어요.")
    private String description;

    @Schema(description = "회원 역할", example = "USER")
    private Role role;

    @Schema(description = "현재까지 작성한 게시글 수", example = "12")
    private long articleCount;

    @Schema(description = "응답 생성 시각", example = "2026-03-28T12:34:56")
    private LocalDateTime createdAt;


    public static MemberResponse fromEntity(Member member, long articleCount) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .birth(member.getBirth())
                .gender(member.getGender())
                .region(member.getRegion())
                .profileImageUrl(member.getProfileImageUrl())
                .description(member.getDescription())
                .role(member.getRole())
                .articleCount(articleCount)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
