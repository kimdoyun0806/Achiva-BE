package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualFriendPushSetting {
    private UUID friendId;
    private String friendNickName;
    private boolean pushEnabled;
}
