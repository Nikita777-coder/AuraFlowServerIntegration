package system.kinescope.controller;

import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import reactor.core.publisher.Mono;
import system.kinescope.dto.KinescopeUploadRequest;
import system.kinescope.dto.KinescopeUploadResponse;
import system.kinescope.dto.KinescopeVideoDataWrapper;
import system.kinescope.service.KinescopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/integration/kinescoope/video")
@RequiredArgsConstructor
public class KinescopeController {
    private final KinescopeService kinescopeService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public KinescopeUploadResponse upload(@Valid @RequestBody KinescopeUploadRequest kinescopeUploadRequest) {
        var ans = kinescopeService.upload(kinescopeUploadRequest);
        return ans;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Mono<KinescopeVideoDataWrapper> get(@RequestParam(name = "video-id") UUID kinescopeVideoId)  {
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
