package system.kinescope.service;

import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import system.kinescope.dto.KinescopeUploadRequest;
import system.kinescope.dto.KinescopeUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import system.kinescope.dto.KinescopeVideoDataWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KinescopeService {
    private final RestService restService;

    @Value("${service-configs.kinescope.load-video-url}")
    private String kinescopeLoadVideoUrl;

    @Value("${service-configs.kinescope.v1-video-url}")
    private String kinescopeV1VideoUrl;

    @Value("${service-configs.kinescope.token}")
    private String kinescopeToken;

    @Value("${service-configs.kinescope.project-id}")
    private String kinescopeProjectId;
    public KinescopeUploadResponse upload(KinescopeUploadRequest kinescopeUploadRequest) {
        if (kinescopeUploadRequest.getUploadVideo() == null && kinescopeUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        Map<String, String> headers = getDefaultHeaders();
        headers.put("X-Video-Title", kinescopeUploadRequest.getTitle());
        headers.put("X-Video-Description", kinescopeUploadRequest.getDescription());

        if (kinescopeUploadRequest.getUploadVideo() != null) {
            headers.put("X-File-Name", "meditation.mp4");
            return restService.post(kinescopeLoadVideoUrl, headers, kinescopeUploadRequest.getUploadVideo(), KinescopeUploadResponse.class);
        }

        headers.put("X-Video-URL", kinescopeUploadRequest.getSourceLink());
        return restService.post(kinescopeLoadVideoUrl, headers, KinescopeUploadResponse.class);
    }
    public Mono<KinescopeVideoDataWrapper> get(UUID videoId) {
        Map<String, String> headers = getDefaultHeaders();

        return restService.get(
                getUriWithVideoIdAsPathVariable(kinescopeV1VideoUrl, videoId),
                headers,
                KinescopeVideoDataWrapper.class);
    }
    public void delete(UUID videoId) {
        Map<String, String> headers = getDefaultHeaders();

        restService.delete(getUriWithVideoIdAsPathVariable(kinescopeV1VideoUrl, videoId), headers);
    }
    private Map<String, String> getDefaultHeaders() {
        return new HashMap<>() {{
                put("X-Parent-ID", kinescopeProjectId);
                put("Authorization", String.format("Bearer %s", kinescopeToken));
        }};
    }
    private String getUriWithVideoIdAsPathVariable(String url, UUID videoId) {
        return UriComponentsBuilder.fromUriString(url)
                .pathSegment("{videoId}")
                .buildAndExpand(videoId)
                .toUriString();
    }
}
