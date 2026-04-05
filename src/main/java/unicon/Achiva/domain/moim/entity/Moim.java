package unicon.Achiva.domain.moim.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Builder.Default
    @Column(nullable = false)
    private int score = 0;

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

    public void increaseScore() {
        this.score += 1;
    }

    public void decreaseScore() {
        if (this.score > 0) {
            this.score -= 1;
        }
    }

    public void update(String name,
                       String description,
                       Integer maxMember,
                       Boolean isPrivate,
                       String password,
                       Boolean isOfficial,
                       Integer targetAmount,
                       Integer pokeDays) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (maxMember != null) {
            this.maxMember = maxMember;
        }
        if (isOfficial != null) {
            this.isOfficial = isOfficial;
        }
        if (targetAmount != null) {
            this.targetAmount = targetAmount;
        }
        if (pokeDays != null) {
            this.pokeDays = pokeDays;
        }

        if (isPrivate != null) {
            this.isPrivate = isPrivate;
            if (!isPrivate) {
                this.password = null;
            }
        }

        if (password != null && this.isPrivate && !password.isBlank()) {
            this.password = password;
        }
    }
}
