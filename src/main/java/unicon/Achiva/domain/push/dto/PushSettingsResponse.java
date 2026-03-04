package unicon.Achiva.domain.push.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSettingsResponse {
    private boolean totalPushEnabled;
    private boolean friendPostPushEnabled;
    private List<IndividualFriendPushSetting> individualFriendSettings;
}
