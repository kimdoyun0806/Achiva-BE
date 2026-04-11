package unicon.Achiva.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import unicon.Achiva.domain.member.Gender;
import unicon.Achiva.domain.member.entity.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "회원가입 응답")
public class CreateMemberResponse {
    @org.hibernate.validator.constraints.UUID
    @Schema(description = "회원 UUID")
    private UUID id;
    @Email
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    @Size(min = 2, max = 255)
    @Schema(description = "닉네임", example = "achiva_user#1234")
    private String nickName;
    @URL(protocol = "https")
    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;
    @Schema(description = "생년월일", example = "2000-01-01")
    private LocalDate birth;
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
    @Size(min = 2, max = 100)
    @Schema(description = "지역", example = "서울")
    private String region;
    @Schema(description = "소속 Organization ID", example = "1")
    private Long organizationId;
    @Schema(description = "소속 Organization 이름", example = "Achiva University")
    private String organizationName;
    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;

    public static CreateMemberResponse fromEntity(Member member) {
        return CreateMemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .profileImageUrl(member.getProfileImageUrl())
                .birth(member.getBirth())
                .gender(member.getGender() != null ? member.getGender() : null)
                .region(member.getRegion() != null ? member.getRegion() : null)
                .organizationId(member.getOrganization().getId())
                .organizationName(member.getOrganization().getName())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
