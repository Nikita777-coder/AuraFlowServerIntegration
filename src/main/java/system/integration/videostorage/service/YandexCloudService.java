package system.integration.videostorage.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import system.integration.videostorage.dto.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class YandexCloudService {
    private final KinescopeService kinescopeService;
    private final Path root = Paths.get("uploads");
    private final Map<UUID, VideoStorageUploadResponse> videoStatuses = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${service-configs.yandex-cloud.endpoint}")
    private String ENDPOINT;

    @Value("${service-configs.yandex-cloud.bucket-name}")
    private String BUCKET_NAME;

    @Value("${service-configs.yandex-cloud.video-folder}")
    private String FOLDER_NAME;

    @Value("${service-configs.yandex-cloud.access-key}")
    private String ACCESS_KEY;

    @Value("${service-configs.yandex-cloud.secret-key}")
    private String SECRET_KEY;
    private S3Client s3;

    @PostConstruct
    private void initS3() {
        s3 = S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .region(Region.of("ru-central1"))
                .build();
    }
    public UUID upload(VideoStorageUploadRequest videoStorageUploadRequest) {
        if (videoStorageUploadRequest.getUploadVideo() == null && videoStorageUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        UUID taskId = UUID.randomUUID();
        videoStatuses.put(taskId, new VideoStorageUploadResponse());

        if (videoStorageUploadRequest.getUploadVideo() != null) {
            executorService.submit(() -> uploadVideo(taskId, videoStorageUploadRequest));
            return taskId;
        }

        executorService.submit(() -> uploadByLink(taskId, videoStorageUploadRequest));
        return taskId;
    }
    public VideoStorageUploadResponse getData(UUID id) {
        var shallowCopy = videoStatuses.get(id);
        if (videoStatuses.get(id).getStatus() == Status.READY) {
            shallowCopy = new VideoStorageUploadResponse();

            shallowCopy.setStatus(Status.valueOf(videoStatuses.get(id).getStatus().toString()));
            shallowCopy.setWasUploadFromUrl(videoStatuses.get(id).isWasUploadFromUrl());
            shallowCopy.setUploadResponse(new KinescopeUploadResponse());
            shallowCopy.getUploadResponse().setData(new KinescopeData());
            shallowCopy.getUploadResponse().getData().setTitle(videoStatuses.get(id).getUploadResponse().getData().getTitle());
            shallowCopy.getUploadResponse().getData().setId(UUID.fromString(videoStatuses.get(id).getUploadResponse().getData().getId().toString()));
            shallowCopy.getUploadResponse().getData().setDescription(videoStatuses.get(id).getUploadResponse().getData().getDescription());
            shallowCopy.getUploadResponse().getData().setStatus(videoStatuses.get(id).getUploadResponse().getData().getStatus());
            shallowCopy.getUploadResponse().getData().setEmbedLink(videoStatuses.get(id).getUploadResponse().getData().getEmbedLink());
            shallowCopy.getUploadResponse().getData().setCreatedAt(LocalDateTime.parse(videoStatuses.get(id).getUploadResponse().getData().getCreatedAt().toString()));

            videoStatuses.remove(id);
        }

        return shallowCopy;
    }
    private void loadFromKinescope(VideoStorageUploadResponse data) {
        for (var value: videoStatuses.entrySet()) {
            if (value.getValue().getStatus() == Status.PARSED) {
                var d = kinescopeService.getWithAdditionalInfo(data.getUploadResponse().getData().getId());

                if (d.getData().getAssets().size() < 2) {
                    throw new IllegalArgumentException("meditation is not uploaded");
                }

                InputStream in;
                try {
                    in = new URL(d.getData().getAssets().get(1).getDownloadLink()).openStream();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (!Files.exists(root)) {
                    try {
                        Files.createDirectories(root);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Получаем имя файла
                String fileName = data.getUploadResponse().getData().getTitle();
                if (!fileName.contains(".mp4")) {
                    fileName += ".mp4";
                }

                Path targetLocation = root.resolve(fileName);

                copyFileByLink(in, targetLocation, value.getKey());

                kinescopeService.delete(data.getUploadResponse().getData().getId());
            }
        }
    }
    public void delete(String link, UUID id) {
        s3.deleteObject(DeleteObjectRequest
                .builder()
                .bucket(BUCKET_NAME)
                .key(extractObjectKeyFromLink(link))
                .build()
        );

        if (id != null) {
            kinescopeService.delete(id);
        }
    }
    public String getTry(String link) {
        try (var responseInputStream = s3.getObject(
                GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(extractObjectKeyFromLink(link))
                        .build()
        )) {
            return "success";
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException("no such meditation");
        } catch (IOException ioEx) {
            throw new RuntimeException("error reading object", ioEx);
        }
    }
    private void uploadVideo(UUID taskId, VideoStorageUploadRequest videoStorageUploadRequest) {
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String fileName = videoStorageUploadRequest.getUploadVideo().getOriginalFilename();
        Path targetLocation = root.resolve(fileName);

        try {
            copyFile(videoStorageUploadRequest.getUploadVideo().getInputStream(), targetLocation, taskId);
            videoStatuses.get(taskId).getUploadResponse().getData().setTitle(videoStorageUploadRequest.getTitle());

            if (videoStorageUploadRequest.getDescription() != null) {
                videoStatuses.get(taskId).getUploadResponse().getData().setDescription(videoStorageUploadRequest.getDescription());
            }

            videoStatuses.get(taskId).setStatus(Status.READY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyFile(
            InputStream in,
            Path targetLocation,
            UUID taskId
    ) {
        try {
            videoStatuses.get(taskId).setStatus(Status.SYSTEM_FILE_COPYING);
            Files.copy(in, targetLocation);
        } catch (FileAlreadyExistsException ex) {

        }
        catch (IOException e) {
            videoStatuses.get(taskId).setStatus(Status.ERROR);
            throw new RuntimeException(e);
        }

        File videoFile = new File(targetLocation.toString());
        videoStatuses.get(taskId).getUploadResponse().setData(new KinescopeData());
        videoStatuses.get(taskId).getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, videoFile.getName())
        );

        upload(videoFile, taskId);
    }

    private void copyFileByLink(
            InputStream in,
            Path targetLocation,
            UUID taskId
    ) {
        try {
            videoStatuses.get(taskId).setStatus(Status.SYSTEM_FILE_COPYING);
            Files.copy(in, targetLocation);
        } catch (FileAlreadyExistsException ex) {

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        File videoFile = new File(targetLocation.toString());
        uploadFromLink(videoFile, taskId);
    }

    private void uploadByLink(UUID taskId, VideoStorageUploadRequest videoStorageUploadRequest) {
        videoStatuses.get(taskId).setStatus(Status.PARSING);
        var b = kinescopeService.upload(videoStorageUploadRequest);

        String fileName = videoStorageUploadRequest.getTitle();
        if (!fileName.contains(".mp4")) {
            fileName += ".mp4";
        }

        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, fileName)
        );
        b.getUploadResponse().getData().setTitle(videoStorageUploadRequest.getTitle());
        b.setStatus(Status.PARSED);
        videoStatuses.put(taskId, b);
    }
    private void upload(File videoFile, UUID taskId) {
        String objectKey = FOLDER_NAME + "/" + videoFile.getName();
        videoStatuses.get(taskId).setStatus(Status.LOADING_TO_STORAGE);

       s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );
    }
    private void uploadFromLink(File videoFile, UUID taskId) {
        videoStatuses.get(taskId).setStatus(Status.LOADING_TO_STORAGE);
        String objectKey = FOLDER_NAME + "/" + videoFile.getName();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );

        videoStatuses.get(taskId).setStatus(Status.READY);
    }
    private String extractObjectKeyFromLink(String link) {
        var ob =  link.split(ENDPOINT + '/' + BUCKET_NAME + '/');

        if (ob.length < 2) {
            throw new IllegalArgumentException("illegal link");
        }

        return ob[ob.length - 1];
    }
}
