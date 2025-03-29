package system.integration.videostorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.service.YandexCloudService;

@RestController
@RequestMapping("/integration/video-storage/yandexcloud")
@RequiredArgsConstructor
public class YandexCloudController {
    private final YandexCloudService yandexCloudService;
//    @PostMapping("/by-url")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ResponseBody
//    public String uploadByUrl(@Valid @RequestBody VideoStorageUploadRequest kinescopeUploadRequest) {
//        var ans = yandexCloudService.upload(kinescopeUploadRequest);
//        return ans;
//    }
//
//    @PostMapping("/by-upload-video")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ResponseBody
//    public String uploadByLocalVideo(@RequestParam String title,
//                                     @RequestParam("upload-video") MultipartFile file,
//                                     @RequestParam(required = false) String description) {
//        var ans = yandexCloudService.upload(
//                VideoStorageUploadRequest.builder()
//                        .title(title)
//                        .uploadVideo(file)
//                        .description(description)
//                        .build()
//        );
//
//        return ans;
//    }
}
