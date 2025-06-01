package system.integration.videostorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoStorageUploadResponse {
    private KinescopeUploadResponse uploadResponse;
    private boolean wasUploadFromUrl;
    private Status status;
    
    public static VideoStorageUploadResponse getShallowCopy(VideoStorageUploadResponse videoStorageUploadResponse) {
        VideoStorageUploadResponse shallowCopy = new VideoStorageUploadResponse();
        
        shallowCopy.setStatus(Status.valueOf(videoStorageUploadResponse.getStatus().toString()));
        shallowCopy.setWasUploadFromUrl(videoStorageUploadResponse.isWasUploadFromUrl());
        shallowCopy.setUploadResponse(new KinescopeUploadResponse());
        shallowCopy.getUploadResponse().setData(new KinescopeData());
        shallowCopy.getUploadResponse().getData().setTitle(videoStorageUploadResponse.getUploadResponse().getData().getTitle());
        shallowCopy.getUploadResponse().getData().setEmbedLink(videoStorageUploadResponse.getUploadResponse().getData().getEmbedLink());
        shallowCopy.getUploadResponse().getData().setDescription(videoStorageUploadResponse.getUploadResponse().getData().getDescription());
        shallowCopy.getUploadResponse().getData().setStatus(videoStorageUploadResponse.getUploadResponse().getData().getStatus());

        if (videoStorageUploadResponse.getUploadResponse().getData().getId() != null) {
            shallowCopy.getUploadResponse().getData().setId(UUID.fromString(videoStorageUploadResponse.getUploadResponse().getData().getId().toString()));
        }

        if (videoStorageUploadResponse.getUploadResponse().getData().getCreatedAt() != null) {
            shallowCopy.getUploadResponse().getData().setCreatedAt(LocalDateTime.parse(videoStorageUploadResponse.getUploadResponse().getData().getCreatedAt().toString()));
        }

        return shallowCopy;
    }
}
