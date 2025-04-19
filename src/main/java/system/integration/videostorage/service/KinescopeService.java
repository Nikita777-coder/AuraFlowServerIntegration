package system.integration.videostorage.service;

import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import system.service.RestService;
import system.integration.videostorage.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    public VideoStorageUploadResponse upload(VideoStorageUploadRequest kinescopeUploadRequest) {
        if (kinescopeUploadRequest.getUploadVideo() == null && kinescopeUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        Map<String, String> headers = getDefaultHeaders();
        headers.put("X-Video-Title", kinescopeUploadRequest.getTitle());
        headers.put("X-Video-Description", kinescopeUploadRequest.getDescription());

        if (kinescopeUploadRequest.getUploadVideo() != null) {
            headers.put("X-File-Name", "meditation.mp4");
            return new VideoStorageUploadResponse(
                    restService.post(kinescopeLoadVideoUrl, headers, kinescopeUploadRequest.getUploadVideo(), KinescopeUploadResponse.class),
                    false
            );
        }

        headers.put("X-Video-URL", kinescopeUploadRequest.getSourceLink());
        return new VideoStorageUploadResponse(
                restService.post(kinescopeLoadVideoUrl, headers, KinescopeUploadResponse.class),
                true
        );
    }
    public Mono<KinescopeVideoDataWrapper> get(UUID videoId) {
        Map<String, String> headers = getDefaultHeaders();

        return restService.get(
                getUriWithVideoIdAsPathVariable(kinescopeV1VideoUrl, videoId),
                headers,
                KinescopeVideoDataWrapper.class);
    }
    public KinescopeGetResponse getWithAdditionalInfo(UUID videoId) {
        Map<String, String> headers = getDefaultHeaders();

        return restService.getWithoutMono(
                getUriWithVideoIdAsPathVariable(kinescopeV1VideoUrl, videoId),
                headers,
                KinescopeGetResponse.class);
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
