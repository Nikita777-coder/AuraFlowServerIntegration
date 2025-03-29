package system.integration.videostorage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

// one of url or uploadedVideo must be not null
@Getter
@Setter
@AllArgsConstructor
@Builder
public class VideoStorageUploadRequest {
    @NotBlank(message = "can't be empty")
    private String title;
    private String description;
    private String sourceLink;
    private MultipartFile uploadVideo;
}
