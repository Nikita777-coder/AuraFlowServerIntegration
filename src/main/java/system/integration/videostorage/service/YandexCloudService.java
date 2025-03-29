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
import system.integration.videostorage.dto.VideoStorageUploadRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class YandexCloudService {
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
    public String upload(VideoStorageUploadRequest videoStorageUploadRequest) {
        if (videoStorageUploadRequest.getUploadVideo() == null && videoStorageUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        if (videoStorageUploadRequest.getUploadVideo() != null) {
            return uploadVideo(videoStorageUploadRequest);
        }

        return uploadByLink(videoStorageUploadRequest);
    }
    private String uploadVideo(VideoStorageUploadRequest videoStorageUploadRequest) {
        File videoFile = new File(videoStorageUploadRequest.getUploadVideo().getOriginalFilename());
        return upload(videoFile);
    }

    private String uploadByLink(VideoStorageUploadRequest videoStorageUploadRequest) {
        String outputPath = String.format("videos/%s", videoStorageUploadRequest.getTitle());
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "yt-dlp", "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]", "-o", outputPath, videoStorageUploadRequest.getSourceLink()
            );
            processBuilder.directory(new File("."));
            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return upload(new File(outputPath));
            } else {
                throw new RuntimeException("exit code is not equal to 0");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("unknown error");
    }
    private String upload(File videoFile) {
        String objectKey = "videos/" + videoFile.getName();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );
        return String.format("%s/%s/%s.mp4", ENDPOINT, BUCKET_NAME, videoFile.getName());
    }
}
