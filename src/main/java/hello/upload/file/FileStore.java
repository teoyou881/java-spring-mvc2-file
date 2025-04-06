package hello.upload.file;

import hello.upload.domain.UploadFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class FileStore {

  @Value("${file.dir}")
  private String fileDir;


  public String getFullPath(String filename) {
    return fileDir + filename;
  }

  public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
    List<UploadFile> storeFileResult = new ArrayList<>();
    for (MultipartFile multipartFile : multipartFiles) {
      if (!multipartFile.isEmpty()) {
        storeFileResult.add(storeFile(multipartFile));
      }
    }
    return storeFileResult;
  }

  public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
    if (multipartFile.isEmpty()) {
      return null;
    }

    String originalFilename = multipartFile.getOriginalFilename();
    String storeFileName = createStoreFileName(multipartFile);
    multipartFile.transferTo(new File(getFullPath(storeFileName)));
    return new UploadFile(originalFilename, storeFileName);
  }

  //helper
  private static String createStoreFileName(MultipartFile multipartFile) {
    String ext = Optional.ofNullable(multipartFile.getOriginalFilename())
                         .map(FileStore::extractExt)
                         .orElseThrow(() -> new IllegalArgumentException("Filename is missing"));
    String uuid = UUID.randomUUID()
                      .toString();
    return uuid + "." + ext;
  }

  private static String extractExt(String originalFilename) {
    int pos = originalFilename.lastIndexOf(".");
    return originalFilename.substring(pos + 1);
  }
}
