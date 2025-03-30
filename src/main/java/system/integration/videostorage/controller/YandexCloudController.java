package system.integration.videostorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.dto.YandexCloudUploadResponse;
import system.integration.videostorage.service.YandexCloudService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/integration/video-storage/yandexcloud")
@RequiredArgsConstructor
public class YandexCloudController {
    private final YandexCloudService yandexCloudService;
    @PostMapping("/by-url")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public YandexCloudUploadResponse uploadByUrl(@Valid @RequestBody VideoStorageUploadRequest kinescopeUploadRequest) {
        var ans = yandexCloudService.upload(kinescopeUploadRequest);
        return ans;
    }

    @PostMapping(
            value = "/by-upload-video",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public YandexCloudUploadResponse uploadByLocalVideo(@RequestParam String title,
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

//    @PostMapping(
//            name = "/upload",
//            consumes = "multipart/form-data"
//    )
//    public ResponseEntity<String> uploadVideo(@RequestParam MultipartFile file) {
//        try {
//            // Проверяем, существует ли директория, если нет - создаём её
//            if (!Files.exists(root)) {
//                Files.createDirectories(root); // Создаём директорию, если она не существует
//            }
//
//            // Получаем имя файла
//            String fileName = file.getOriginalFilename();
//            Path targetLocation = root.resolve(fileName); // Полный путь к файлу
//
//            // Копируем файл в целевую директорию
//            Files.copy(file.getInputStream(), targetLocation);
//
//            return ResponseEntity.ok("Video uploaded successfully: " + fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(500).body("Error uploading video: " + e.getMessage());
//        }
//    }
}
