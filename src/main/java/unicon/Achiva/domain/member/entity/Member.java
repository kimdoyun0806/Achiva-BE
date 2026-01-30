package unicon.Achiva.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.URL;
import unicon.Achiva.domain.article.entity.Article;
import unicon.Achiva.domain.auth.Role;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.member.Gender;
import unicon.Achiva.global.common.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickName;

    @URL(protocol = "https")
    @Builder.Default
    private String profileImageUrl = "https://achivadata.s3.ap-northeast-2.amazonaws.com/default-profile-image.png";

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String region;

    @Column(length = 500)
    private String description;

    @ElementCollection(targetClass = Category.class)
    @Enumerated(EnumType.STRING)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Column(columnDefinition = "varchar(50)")
    private List<Category> categories;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private Role role;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();

    @Builder.Default
    private boolean pushEnabled = true;

    public void updatePushEnabled(boolean enabled) {
        this.pushEnabled = enabled;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void updateBirth(LocalDate parse) {
        this.birth = parse;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateCategories(List<Category> list) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        this.categories.clear();
        this.categories.addAll(list);
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}