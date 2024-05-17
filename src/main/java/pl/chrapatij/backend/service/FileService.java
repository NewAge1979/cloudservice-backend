package pl.chrapatij.backend.service;

import org.springframework.web.multipart.MultipartFile;
import pl.chrapatij.backend.dto.FileDownloadDto;
import pl.chrapatij.backend.dto.FileListDto;
import pl.chrapatij.backend.entity.File;

import java.util.List;

public interface FileService {
    List<FileListDto> getFileList(String token, int limit) ;

    File fileUpload(String token, String filename, MultipartFile file);

    File fileRename(String token, String oldFileName, String newFileName);

    File fileRemove(String token, String filename);

    FileDownloadDto fileDownload(String token, String filename);
}