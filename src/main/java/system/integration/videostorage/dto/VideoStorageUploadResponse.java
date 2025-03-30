package system.integration.videostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VideoStorageUploadResponse {
    private KinescopeUploadResponse uploadResponse;
    private boolean wasUploadFromUrl;
}
