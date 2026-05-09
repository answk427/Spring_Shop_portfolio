package work.trade.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileUploadService implements FileUploadService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws FileUploadException {
        try {
            String originalFilename = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String filename = uuid + "_" + originalFilename;

            File directory = new File(uploadDir + "/", folder);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File destinationFile = new File(directory, filename);
            file.transferTo(destinationFile);

            String imageUrl = "/" + folder + "/" + filename;
            log.info("Local file uploaded: {}", imageUrl);

            return imageUrl;
        } catch (IOException e) {
            throw new FileUploadException("파일 업로드 실패: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String imageUrl) {
        try {
            File file = new File(uploadDir + imageUrl);
            if (file.exists()) {
                file.delete();
                log.info("Local file deleted: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", imageUrl, e);
        }
    }
}
