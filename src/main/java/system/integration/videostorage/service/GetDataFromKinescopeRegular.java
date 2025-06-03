package system.integration.videostorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import system.integration.videostorage.DTOMapper;
import system.integration.videostorage.dto.KinescopeUploadResponse;
import system.integration.videostorage.dto.Status;
import system.integration.videostorage.dto.VideoStorageUploadResponse;

import java.time.Duration;
import java.util.UUID;

@Service
@EnableAsync
@RequiredArgsConstructor
public class GetDataFromKinescopeRegular {
    public final KinescopeService kinescopeService;
    public final YandexCloudService yandexCloudService;
    public final DTOMapper dtoMapper;

    @Async
    @Scheduled(fixedRateString = "${server.integration.fixed-rate-time}")
    public void fetchInfoAboutUploadingToKinescope() {
        Flux.fromIterable(KinescopeIds.getAll().entrySet())
                .flatMap(entry -> {
                    UUID videoId = entry.getKey();
                    UUID metadataKey = entry.getValue();

                    return kinescopeService.get(videoId)
                            .filter(wrapper -> "done".equals(wrapper.getData().getStatus()))
                            .flatMap(wrapper -> {
                                VideoStorageUploadResponse shallowCopy =
                                        VideoStorageUploadResponse.getShallowCopy(KinescopeIds.getData(metadataKey));
                                shallowCopy.setStatus(Status.PARSED);
                                KinescopeIds.deleteId(videoId);

                                return Mono.fromRunnable(() ->
                                        yandexCloudService.loadFromKinescope(metadataKey, shallowCopy)
                                );
                            });

                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        success -> {},
                        error -> {
                            System.err.println("❌ Ошибка в процессе загрузки: " + error.getMessage());
                            error.printStackTrace();
                        }
                );
    }
}
