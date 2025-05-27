package system.integration.videostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoStorageUploadResponse {
    private KinescopeUploadResponse uploadResponse;
    private boolean wasUploadFromUrl;
    private Status status;
}
