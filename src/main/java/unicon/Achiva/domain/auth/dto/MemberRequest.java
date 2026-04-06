package unicon.Achiva.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;
import unicon.Achiva.domain.member.Gender;

import java.time.LocalDate;

@Getter
@Builder
@Validated
@Schema(description = "회원가입 요청. organizationId는 필수이며, Organization에 비밀번호가 설정된 경우 organizationPassword도 함께 전달해야 합니다.")
public class MemberRequest {
    @Schema(description = "프로필 이미지 URL", example = "https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png")
    @URL(protocol = "https")
    @Builder.Default
    private String profileImageUrl = "https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png";
    @Schema(description = "닉네임", example = "achiva_user")
    @Size(min = 2, max = 20)
    private String nickName;
    @Schema(description = "생년월일", example = "2000-01-01")
    @NotNull
    private LocalDate birth;
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
    @Schema(description = "지역", example = "서울")
    @Size(max = 50)
    private String region;
    @Schema(description = "가입할 Organization ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long organizationId;
    @Schema(description = "Organization 가입 비밀번호. 해당 Organization이 비밀번호를 요구하지 않으면 생략 가능합니다.", example = "achiva1234")
    private String organizationPassword;
}
