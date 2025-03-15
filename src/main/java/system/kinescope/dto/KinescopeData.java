package system.kinescope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
class KinescopeData {
    private UUID id;
    private String title;
    private String description;
    private String status;

    @JsonProperty("embed_link")
    private String embedLink;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
