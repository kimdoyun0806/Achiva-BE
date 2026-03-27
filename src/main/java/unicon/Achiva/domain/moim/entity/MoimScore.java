package unicon.Achiva.domain.moim.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.LongBaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "moim_score")
public class MoimScore extends LongBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Moim moim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Builder.Default
    @Column(nullable = false)
    private int score = 0;

    private LocalDateTime leftAt;

    public void increaseScore() {
        this.score += 1;
    }

    public void decreaseScore() {
        if (this.score > 0) {
            this.score -= 1;
        }
    }

    public void leave(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }
}
