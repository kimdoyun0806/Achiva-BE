package unicon.Achiva.domain.scripture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.global.common.UuidBaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "member_scripture_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_scripture_progress",
                        columnNames = {"member_id", "scripture_id"}
                )
        }
)
public class MemberScriptureProgress extends UuidBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(name = "scripture_id", nullable = false, length = 50)
    private String scriptureId;

    @Column(name = "completed_chapters", nullable = false)
    private Integer completedChapters;

    public void updateCompletedChapters(Integer completedChapters) {
        this.completedChapters = completedChapters;
    }
}
