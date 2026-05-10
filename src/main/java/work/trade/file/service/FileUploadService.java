package work.trade.file.service;


import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    public String uploadFile(MultipartFile file, String folder) throws FileUploadException;

      void deleteFile(String filePath);
}
