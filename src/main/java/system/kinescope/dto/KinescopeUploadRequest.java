package system.kinescope.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

// one of url or uploadedVideo must be not null
@Getter
@Setter
public class KinescopeUploadRequest {
    @NotBlank(message = "can't be empty")
    private String title;
    private String description;
    private String sourceLink;
    private MultipartFile uploadVideo;
}
