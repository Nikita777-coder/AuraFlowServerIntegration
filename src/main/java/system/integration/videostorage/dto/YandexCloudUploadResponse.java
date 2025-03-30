package system.integration.videostorage.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class YandexCloudUploadResponse {
    private KinescopeUploadResponse kinescopeUploadResponse;
    private boolean wasUploadFromUrl;
    private String link;
}
