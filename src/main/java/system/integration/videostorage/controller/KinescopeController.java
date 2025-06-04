package system.integration.videostorage.controller;

import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.dto.VideoStorageUploadResponse;
import system.integration.videostorage.service.KinescopeService;
import system.integration.videostorage.dto.KinescopeUploadResponse;
import system.integration.videostorage.dto.KinescopeVideoDataWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/video-storage/kinescope")
@RequiredArgsConstructor
public class KinescopeController {
    private final KinescopeService kinescopeService;
    @PostMapping("/by-url")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public VideoStorageUploadResponse uploadByUrl(@Valid @RequestBody VideoStorageUploadRequest kinescopeUploadRequest) {
        var ans = kinescopeService.upload(kinescopeUploadRequest);
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
        var ans = kinescopeService.upload(
                VideoStorageUploadRequest.builder()
                        .title(title)
                        .uploadVideo(file)
                        .description(description)
                        .build()
        );

        return ans;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public KinescopeVideoDataWrapper get(@RequestParam(name = "video-id") UUID kinescopeVideoId)  {
        return kinescopeService.get(kinescopeVideoId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void delete(@RequestParam(name = "video-id") UUID kinescopeVideoId)  {
        kinescopeService.delete(kinescopeVideoId);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
