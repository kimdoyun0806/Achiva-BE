package unicon.Achiva.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.Gender;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Validated
public class MemberRequest {
    @URL(protocol = "https")
    @Builder.Default
    private String profileImageUrl = "https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png";
    @NotNull
    private LocalDate birth;
    private Gender gender;
    @Size(max = 50)
    private String region;
    private List<Category> categories;
}
