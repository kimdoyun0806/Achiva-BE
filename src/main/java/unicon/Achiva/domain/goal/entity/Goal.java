package unicon.Achiva.domain.goal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.UuidBaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "goals")
public class Goal extends UuidBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalCategory category;

    @Column(nullable = false, length = 200)
    private String text;

    @Builder.Default
    @Column(nullable = false)
    private Integer clickCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isArchived = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // Business methods
    public void updateText(String text) {
        this.text = text;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public void toggleArchive() {
        this.isArchived = !this.isArchived;
    }

    public void archive() {
        this.isArchived = true;
    }

    public void unarchive() {
        this.isArchived = false;
    }
}
