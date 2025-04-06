package hello.upload.domain;

import lombok.Data;

@Data
public class UploadFile {

  /*
  * uploadFileName: the original filename uploaded by the user
  * storeFileName: the filename managed internally by the server

    We should not save files on the server using the original filename from the user.
    This is because different users might upload files with the same name, which could lead to name collisions.
    To prevent this, the server must generate and manage a unique internal filename for each uploaded file.
  * */

  private String uploadFileName;
  private String storeFileName;

  public UploadFile(String uploadFileName, String storeFileName) {
    this.uploadFileName = uploadFileName;
    this.storeFileName = storeFileName;
  }
}