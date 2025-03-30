package system.integration.videostorage.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import system.integration.videostorage.dto.KinescopeUploadResponse;
import system.integration.videostorage.dto.KinescopeVideoDataWrapper;
import system.integration.videostorage.dto.VideoStorageUploadRequest;
import system.integration.videostorage.dto.VideoStorageUploadResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class YandexCloudService {
    private final KinescopeService kinescopeService;
    private final Path root = Paths.get("uploads");

    @Value("${service-configs.yandex-cloud.endpoint}")
    private String ENDPOINT;

    @Value("${service-configs.yandex-cloud.bucket-name}")
    private String BUCKET_NAME;

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
    public VideoStorageUploadResponse upload(VideoStorageUploadRequest videoStorageUploadRequest) {
        if (videoStorageUploadRequest.getUploadVideo() == null && videoStorageUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        if (videoStorageUploadRequest.getUploadVideo() != null) {
            return uploadVideo(videoStorageUploadRequest);
        }

        return uploadByLink(videoStorageUploadRequest);
    }
    public String loadFromKinescope(KinescopeVideoDataWrapper data) {
        var d = kinescopeService.getWithAdditionalInfo(data.getData().getId());

        InputStream in = null;
        try {
            in = new URL(d.getKinescopeAssets().get(1).getDownloadLink()).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root); // Создаём директорию, если она не существует
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Получаем имя файла
        String fileName = data.getData().getTitle();
        Path targetLocation = root.resolve("/upload/" + fileName);
        copyFile(in, targetLocation);

        return "success";
    }
    private VideoStorageUploadResponse uploadVideo(VideoStorageUploadRequest videoStorageUploadRequest) {
        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root); // Создаём директорию, если она не существует
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Получаем имя файла
        String fileName = videoStorageUploadRequest.getUploadVideo().getOriginalFilename();
        Path targetLocation = root.resolve("/upload/" + fileName); // Полный путь к файлу

        try {
            return copyFile(videoStorageUploadRequest.getUploadVideo().getInputStream(), targetLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private VideoStorageUploadResponse copyFile(
            InputStream in,
            Path targetLocation
    ) {
        try {
            Files.copy(in, targetLocation);
        } catch (FileAlreadyExistsException ex) {

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        File videoFile = new File(targetLocation.toString());
        return upload(videoFile);
    }

    private VideoStorageUploadResponse uploadByLink(VideoStorageUploadRequest videoStorageUploadRequest) {
        var b = kinescopeService.upload(videoStorageUploadRequest);
        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s", ENDPOINT, BUCKET_NAME, videoStorageUploadRequest.getTitle() + ".mp4")
        );

        return b;
    }
    private VideoStorageUploadResponse upload(File videoFile) {
        String objectKey = "videos/" + videoFile.getName();

        var ans = s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );

        var b = new VideoStorageUploadResponse(
                new KinescopeUploadResponse(),
                false
        );

        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s", ENDPOINT, BUCKET_NAME, videoFile.getName())
        );

        return b;

    }
}
