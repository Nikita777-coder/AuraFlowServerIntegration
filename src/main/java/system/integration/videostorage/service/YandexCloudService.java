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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class YandexCloudService {
    private final KinescopeService kinescopeService;
    private final Path root = Paths.get("uploads");

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
    public VideoStorageUploadResponse upload(VideoStorageUploadRequest videoStorageUploadRequest) {
        if (videoStorageUploadRequest.getUploadVideo() == null && videoStorageUploadRequest.getSourceLink() == null) {
            throw new IllegalArgumentException("you must fill meditation from local storage or provide link to it");
        }

        if (videoStorageUploadRequest.getUploadVideo() != null) {
            return uploadVideo(videoStorageUploadRequest);
        }

        return uploadByLink(videoStorageUploadRequest);
    }
    public VideoStorageUploadResponse loadFromKinescope(VideoStorageUploadResponse data) {
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
        Path targetLocation = root.resolve(fileName);

        copyFileByLink(in, targetLocation);

        kinescopeService.delete(data.getUploadResponse().getData().getId());

        return data;
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
        try {
            s3.getObject(GetObjectRequest
                    .builder()
                    .bucket(BUCKET_NAME)
                    .key(extractObjectKeyFromLink(link))
                    .build()
            );

            return "success";
        } catch (NoSuchKeyException ex) {
            throw new IllegalArgumentException("no such meditation");
        }
    }
    private VideoStorageUploadResponse uploadVideo(VideoStorageUploadRequest videoStorageUploadRequest) {
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

    private void copyFileByLink(
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
        uploadFromLink(videoFile);
    }

    private VideoStorageUploadResponse uploadByLink(VideoStorageUploadRequest videoStorageUploadRequest) {
        var b = kinescopeService.upload(videoStorageUploadRequest);

        if (!videoStorageUploadRequest.getTitle().contains(".mp4")) {
            throw new IllegalArgumentException("not valid format of title, must contain .mp4 in the end");
        }

        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, videoStorageUploadRequest.getTitle())
        );

        return b;
    }
    private VideoStorageUploadResponse upload(File videoFile) {
        String objectKey = FOLDER_NAME + "/" + videoFile.getName();

       s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );

        var kinescopeUploadRequest = new KinescopeUploadResponse();
        kinescopeUploadRequest.setData(new KinescopeData());

        var b = new VideoStorageUploadResponse(
                kinescopeUploadRequest,
                false
        );

        b.getUploadResponse().getData().setEmbedLink(
                String.format("%s/%s/%s/%s", ENDPOINT, BUCKET_NAME, FOLDER_NAME, videoFile.getName())
        );

        return b;
    }
    private void uploadFromLink(File videoFile) {
        String objectKey = FOLDER_NAME + "/" + videoFile.getName();

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(objectKey)
                        .build(),
                Paths.get(videoFile.getAbsolutePath())
        );
    }
    private String extractObjectKeyFromLink(String link) {
        var ob =  link.split(ENDPOINT + '/' + BUCKET_NAME + '/');

        if (ob.length < 2) {
            throw new IllegalArgumentException("illegal link");
        }

        return ob[ob.length - 1];
    }
}
