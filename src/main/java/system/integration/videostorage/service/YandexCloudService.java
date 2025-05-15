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
        if (videoStatuses.get(id) == null) {
            throw new IllegalArgumentException("already got it or not found");
        }

        var shallowCopy = videoStatuses.get(id);
        if (videoStatuses.get(id).getStatus() == Status.READY) {
            shallowCopy = VideoStorageUploadResponse.getShallowCopy(videoStatuses.get(id));
            videoStatuses.remove(id);
        }

        return shallowCopy;
    }
    public void loadFromKinescope(UUID taskId, VideoStorageUploadResponse data) {
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

        copyFileByLink(in, targetLocation, taskId);

        kinescopeService.delete(data.getUploadResponse().getData().getId());
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

        String fileName = videoStorageUploadRequest.getTitle();

        if (!fileName.contains(".mp4")) {
            fileName += ".mp4";
        }

        Path targetLocation = root.resolve(fileName);
        videoStatuses.get(taskId).setUploadResponse(new KinescopeUploadResponse());
        videoStatuses.get(taskId).getUploadResponse().setData(new KinescopeData());

        videoStatuses.get(taskId).getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, fileName)
        );

        try {
            videoStatuses.get(taskId).getUploadResponse().getData().setTitle(videoStorageUploadRequest.getTitle());

            if (videoStorageUploadRequest.getDescription() != null) {
                videoStatuses.get(taskId).getUploadResponse().getData().setDescription(videoStorageUploadRequest.getDescription());
            }

            copyFile(videoStorageUploadRequest.getUploadVideo().getInputStream(), targetLocation, taskId);
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

            File videoFile = new File(targetLocation.toString());
            videoStatuses.get(taskId).getUploadResponse().getData().setEmbedLink(
                    String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, videoFile.getName())
            );

            upload(videoFile, taskId);
            videoFile.delete();
        } catch (FileAlreadyExistsException ex) {
            System.out.println(1);
        }
        catch (Exception ex) {
            videoStatuses.get(taskId).setStatus(Status.ERROR);
            System.out.println(ex);
        }
    }
    private void copyFileByLink(
            InputStream in,
            Path targetLocation,
            UUID taskId
    ) {
        try {
            videoStatuses.get(taskId).setStatus(Status.SYSTEM_FILE_COPYING);
            Files.copy(in, targetLocation);

            File videoFile = new File(targetLocation.toString());
            upload(videoFile, taskId);
            videoFile.delete();
        } catch (FileAlreadyExistsException ex) {
            System.out.println(1);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadByLink(UUID taskId, VideoStorageUploadRequest videoStorageUploadRequest) {
        var b = kinescopeService.upload(videoStorageUploadRequest);

        String fileName = videoStorageUploadRequest.getTitle();
        if (!fileName.contains(".mp4")) {
            fileName += ".mp4";
        }

        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, fileName)
        );
        b.getUploadResponse().getData().setTitle(videoStorageUploadRequest.getTitle());
        b.setStatus(Status.PARSING);
        videoStatuses.put(taskId, b);
        KinescopeIds.addId(b.getUploadResponse().getData().getId(), taskId, b);
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
