package system.integration.videostorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import system.integration.videostorage.DTOMapper;
import system.integration.videostorage.dto.Status;
import system.integration.videostorage.dto.VideoStorageUploadResponse;

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
        for (var val: KinescopeIds.getAll().entrySet()) {
            var ans = kinescopeService.get(val.getKey());

            if (ans != null && ans.getData().getStatus().equals("done")) {
                var shallowCopy = VideoStorageUploadResponse.getShallowCopy(KinescopeIds.getData(val.getValue()));
                shallowCopy.setStatus(Status.PARSED);

                KinescopeIds.deleteId(val.getKey());
                yandexCloudService.loadFromKinescope(val.getValue(), shallowCopy);
            }
        }
    }
}
