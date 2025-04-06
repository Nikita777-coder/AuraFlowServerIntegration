package system.integration.videostorage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KinescopeAsset {
    private String quality;

    @JsonProperty("download_link")
    private String downloadLink;
}
