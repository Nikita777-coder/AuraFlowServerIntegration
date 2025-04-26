package system.integration.onesignal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class OneSignalRequest {
    @JsonProperty("app_id")
    private String appId;

    private Contents contents;

    @JsonProperty("include_player_ids")
    private List<String> includedPlayers;
}