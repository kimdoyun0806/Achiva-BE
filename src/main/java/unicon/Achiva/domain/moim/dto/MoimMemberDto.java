package unicon.Achiva.domain.moim.dto;

import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.moim.entity.MoimMember;
import unicon.Achiva.domain.moim.entity.MoimRole;

import java.util.UUID;

@Getter
@Builder
public class MoimMemberDto {
    private String id;
    private String name;
    private int monthlyPosts;
    private int weeklyStreak;
    private int lastActiveDaysAgo;
    private boolean isMe;
    private MoimRole role;

    public static MoimMemberDto from(MoimMember moimMember, UUID currentUserId, int monthlyPosts, int weeklyStreak) {
        boolean isMe = moimMember.getMember().getId().equals(currentUserId);

        return MoimMemberDto.builder()
                .id(moimMember.getMember().getId().toString())
                .name(moimMember.getMember().getNickName())
                .monthlyPosts(monthlyPosts)
                .weeklyStreak(weeklyStreak)
                .lastActiveDaysAgo(0) // TODO: 실제 마지막 활성화 일수 연동 예정
                .isMe(isMe)
                .role(moimMember.getRole())
                .build();
    }
}

