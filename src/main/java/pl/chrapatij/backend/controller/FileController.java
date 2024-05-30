package pl.chrapatij.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.chrapatij.backend.dto.FileListDto;
import pl.chrapatij.backend.service.FileService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cloud")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileService fileService;

    @GetMapping("/list")
    ResponseEntity<List<FileListDto>> getFileList(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam(required = false) Integer limit
    ) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/list. Method: GET.");

        return ResponseEntity.ok(fileService.getFileList(token, limit));
    }

    @PostMapping("/file")
    ResponseEntity<Void> fileUpload(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam(required = false) String filename,
            @RequestBody MultipartFile file,
            @RequestBody(required = false) String hash
    ) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/file. Method: POST.");
        log.debug("Filename for upload: '{}'", filename);
        fileService.fileUpload(token, filename, file, hash);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/file")
    ResponseEntity<Void> fileRename(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam(required = false) String filename,
            @RequestBody Map<String, String> body
    ) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/file. Method: PUT.");
        log.debug("Filename (old): {} => Filename (new): {}", filename, body.get("filename"));
        fileService.fileRename(token, filename, body.get("filename"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    ResponseEntity<Void> fileRemove(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam String filename
    ) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/file. Method: DELETE.");
        log.debug("Filename for remove: {}", filename);
        fileService.fileRemove(token, filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    ResponseEntity<byte[]> fileDownload(
            @RequestHeader(name = "auth-token") String token,
            @RequestParam(required = false) String filename
    ) {
        log.info("*".repeat(250));
        log.info("Endpoint: cloud/file. Method: GET.");
        log.debug("Filename for download: {}", filename);
        var file = fileService.fileDownload(token, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getType()))
                .contentLength(file.getSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(file.getData());
    }
}