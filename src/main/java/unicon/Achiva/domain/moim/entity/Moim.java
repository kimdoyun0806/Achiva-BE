package unicon.Achiva.domain.moim.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.global.common.LongBaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Moim extends LongBaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private int maxMember;

    @Column(nullable = false)
    private boolean isPrivate;

    private String password;

    @Builder.Default
    @Column(nullable = false)
    private boolean isOfficial = false;

    @Builder.Default
    @Column(nullable = false)
    private int targetAmount = 100;

    @Builder.Default
    @Column(nullable = false)
    private int pokeDays = 5;

    @ElementCollection(targetClass = Category.class)
    @Enumerated(EnumType.STRING)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @CollectionTable(name = "moim_categories", joinColumns = @JoinColumn(name = "moim_id"))
    @Column(columnDefinition = "varchar(50)")
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoimMember> members = new ArrayList<>();
    
    // 현재 가입된 회원 수 계산용 컬럼 또는 메서드. 연관관계로 크기를 구합니다.
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    // 비밀번호 체크
    public boolean checkPassword(String inputPassword) {
        if (!isPrivate) return true;
        return this.password != null && this.password.equals(inputPassword);
    }

    public void updateSettings(int targetAmount, int pokeDays) {
        this.targetAmount = targetAmount;
        this.pokeDays = pokeDays;
    }
}
