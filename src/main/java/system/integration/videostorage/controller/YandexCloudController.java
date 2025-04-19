package system.integration.videostorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.dto.VideoStorageUploadResponse;
import system.integration.videostorage.service.YandexCloudService;

import java.util.UUID;

@RestController
@RequestMapping("/video-storage/yandexcloud")
@RequiredArgsConstructor
public class YandexCloudController {
    private final YandexCloudService yandexCloudService;
    @PostMapping("/by-url")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public VideoStorageUploadResponse uploadByUrl(@Valid @RequestBody VideoStorageUploadRequest kinescopeUploadRequest) {
        var ans = yandexCloudService.upload(kinescopeUploadRequest);
        return ans;
    }

    @PostMapping(
            value = "/by-upload-video",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public VideoStorageUploadResponse uploadByLocalVideo(@RequestParam String title,
                                     @RequestParam("upload-video") MultipartFile file,
                                     @RequestParam(required = false) String description) {
        var ans = yandexCloudService.upload(
                VideoStorageUploadRequest.builder()
                        .title(title)
                        .uploadVideo(file)
                        .description(description)
                        .build()
        );

        return ans;
    }

    @PostMapping("/upload-from-kinescope")
    @ResponseBody
    public VideoStorageUploadResponse uploadLoadedVideoFromKinescopeToYandex(@RequestBody VideoStorageUploadResponse data) {
        var ans = yandexCloudService.loadFromKinescope(data);
        return ans;
    }

    @DeleteMapping
    @ResponseBody
    public void deleteVideoByLink(@RequestParam(name = "video-link") String link,
                                    @RequestParam(name = "video-id", required = false) UUID id) {
        yandexCloudService.delete(link, id);
    }

    @GetMapping
    @ResponseBody
    public String tryGetVideoByLink(@RequestParam(name = "video-link") String link) {
        return yandexCloudService.getTry(link);
    }
}
