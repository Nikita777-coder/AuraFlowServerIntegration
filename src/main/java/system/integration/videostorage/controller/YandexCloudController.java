package system.integration.videostorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.dto.VideoStorageUploadResponse;
import system.integration.videostorage.service.YandexCloudService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/video-storage/yandexcloud")
@RequiredArgsConstructor
public class YandexCloudController {
    private final YandexCloudService yandexCloudService;
    @PostMapping("/by-url")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UUID uploadByUrl(@Valid @RequestBody VideoStorageUploadRequest kinescopeUploadRequest) {
        var ans = yandexCloudService.upload(kinescopeUploadRequest);
        return ans;
    }

    @PostMapping(
            value = "/by-upload-video",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UUID uploadByLocalVideo(@RequestParam String title,
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

    @GetMapping("/get-data-info")
    @ResponseBody
    public VideoStorageUploadResponse getData(@RequestParam("task-id") UUID taskId) {
        return yandexCloudService.getData(taskId);
    }

    @DeleteMapping
    @ResponseBody
    public void deleteVideoByLink(@RequestParam(name = "video-link") String link) {
        yandexCloudService.delete(link);
    }

    @GetMapping
    @ResponseBody
    public String tryGetVideoByLink(@RequestParam(name = "video-link") String link) {
        var ans = yandexCloudService.getTry(link);
        return ans;
    }

    @GetMapping("/all")
    @ResponseBody
    public List<String> getAllPlatformVideos()  {
        return yandexCloudService.getAllLinksPlatformVideos();
    }
}
