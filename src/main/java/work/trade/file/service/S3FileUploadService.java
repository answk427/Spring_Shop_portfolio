package work.trade.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(
        name = "file.storage.type",
        havingValue = "s3"
)
public class S3FileUploadService implements FileUploadService{

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws FileUploadException {
        try {
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String filename = uuid + "_" + originalFilename;

            // S3 key (경로)
            String key = folder + "/" + filename;

            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            // S3 URL 생성
            String cloudFrontUrl = String.format("https://%s/%s",
                    cloudFrontDomain, key);

            log.info("S3 file uploaded: {}", cloudFrontUrl);

            //key만 반환
            return key;

        } catch (IOException e) {
            throw new FileUploadException("S3 업로드 실패: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 file deleted: {}", key);

        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", key, e);
        }
    }
}
