package unicon.Achiva.domain.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "member_category_counter")
@Getter
@Setter
@NoArgsConstructor
public class MemberCategoryCounter {

    @EmbeddedId
    private MemberCategoryKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Version
    private long version;

    @Column(nullable = false)
    private long size = 0L;
}