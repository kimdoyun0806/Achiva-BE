package unicon.Achiva.domain.auth.dto;

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
public class CreateMemberResponse {
    @org.hibernate.validator.constraints.UUID
    private UUID id;
    @Email
    private String email;
    @Size(min = 2, max = 20)
    private String nickName;
    @URL(protocol = "https")
    private String profileImageUrl;
    private LocalDate birth;
    private Gender gender;
    @Size(min = 2, max = 100)
    private String region;
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
                .createdAt(member.getCreatedAt())
                .build();
    }
}
