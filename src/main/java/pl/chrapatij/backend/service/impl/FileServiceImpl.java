package pl.chrapatij.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.chrapatij.backend.dto.FileDownloadDto;
import pl.chrapatij.backend.dto.FileListDto;
import pl.chrapatij.backend.entity.File;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.exception.userExceptionError500;
import pl.chrapatij.backend.mapper.FileDownloadMapper;
import pl.chrapatij.backend.mapper.FileListMapper;
import pl.chrapatij.backend.repository.FileRepository;
import pl.chrapatij.backend.security.JwtService;
import pl.chrapatij.backend.service.FileService;
import pl.chrapatij.backend.service.UserService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final FileListMapper fileListMapper;
    private final FileDownloadMapper fileDownloadMapper;

    @Override
    public List<FileListDto> getFileList(String token, Integer limit) {
        if (limit == null || limit == 0) {
            throw new userExceptionError400("The limit value must be greater than 0.");
        }
        try {
            var userId = userService.findByLogin(jwtService.getLogin(token)).getId();
            return fileListMapper.toDtos(fileRepository.findFilesByUserWithLimit(userId, limit));
        } catch (Exception e) {
            throw new userExceptionError500("Error getting file list");
        }
    }

    @Override
    public File fileUpload(String token, String filename, MultipartFile file, String hash) {
        var user = userService.findByLogin(jwtService.getLogin(token));

        if (file == null || file.isEmpty()) {
            throw new userExceptionError400("Error input data.");
        }

        byte[] data;
        try {
            log.debug("Hash: {}", hash);
            data = file.getBytes();
        } catch (IOException e) {
            throw new userExceptionError500(e.getMessage());
        }

        if (hash == null || hash.isEmpty()) {
            hash = UUID.randomUUID().toString();
        }

        fileRepository.findByNameAndUserIdAndIsDeleted(filename, user.getId(), false).ifPresent(
                t -> sendFileError("Upload", filename, true)
        );
        fileRepository.findByHashAndUserIdAndIsDeleted(hash, user.getId(), false).ifPresent(
                t -> sendFileError("Upload", filename, true)
        );

        File newFile = File.builder()
                .name(filename)
                .type(file.getContentType())
                .size(file.getSize())
                .data(data)
                .hash(hash)
                .user(user)
                .isDeleted(false)
                .build();
        fileRepository.save(newFile);
        return newFile;
    }

    @Override
    public File fileRename(String token, String oldFileName, String newFileName) {
        var user = userService.findByLogin(jwtService.getLogin(token));

        Optional<File> fileForRename = fileRepository.findByNameAndUserIdAndIsDeleted(oldFileName, user.getId(), false);
        if (fileForRename.isPresent()) {
            File file = fileForRename.get();
            file.setName(newFileName);
            fileRepository.save(file);
            return file;
        } else {
            sendFileError("Rename", oldFileName, false);
            return null;
        }
    }

    @Override
    public File fileRemove(String token, String filename) {
        var user = userService.findByLogin(jwtService.getLogin(token));

        Optional<File> fileForRemove = fileRepository.findByNameAndUserIdAndIsDeleted(filename, user.getId(), false);
        if (fileForRemove.isPresent()) {
            File file = fileForRemove.get();
            fileRepository.removeById(file.getId());
            return file;
        } else {
            sendFileError("Remove", filename, false);
            return null;
        }
    }

    @Override
    public FileDownloadDto fileDownload(String token, String filename) {
        var user = userService.findByLogin(jwtService.getLogin(token));

        Optional<File> fileForDownload = fileRepository.findByNameAndUserIdAndIsDeleted(filename, user.getId(), false);
        if (fileForDownload.isPresent()) {
            return fileDownloadMapper.toDto(fileForDownload.get());
        } else {
            sendFileError("Download", filename, false);
            return null;
        }
    }

    private void sendFileError(String operation, String filename, boolean exists) {
        log.debug("{} file: File {} {} exists.", operation, filename, (exists ? "already" : "not"));
        throw new userExceptionError400(String.format("File %s %s exists.", filename, (exists ? "already" : "not")));
    }
}