package pl.chrapatij.backend.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.chrapatij.backend.dto.FileDownloadDto;
import pl.chrapatij.backend.dto.FileListDto;
import pl.chrapatij.backend.entity.File;
import pl.chrapatij.backend.entity.Role;
import pl.chrapatij.backend.entity.User;
import pl.chrapatij.backend.exception.userExceptionError400;
import pl.chrapatij.backend.mapper.FileDownloadMapper;
import pl.chrapatij.backend.mapper.FileListMapper;
import pl.chrapatij.backend.repository.FileRepository;
import pl.chrapatij.backend.security.JwtService;
import pl.chrapatij.backend.service.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {
    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private FileListMapper fileListMapper;
    @Mock
    private FileDownloadMapper fileDownloadMapper;
    private final String LOGIN = "test@cloud.ru";
    private final String PASSWORD = "test";
    private final Set<Role> roles = Set.of(Role.builder().code("ROLE_TEST").build());
    @Mock
    private final User user = User.builder().id(1L).login(LOGIN).password(PASSWORD).roles(roles).build();
    private final String FILENAME = "test1.txt";
    private final String TEXT = "Text";
    private final String HASH = "71988c4d8e0803ba4519f0b2864c1331c14a1890bf8694e251379177bfedb5c3";
    private final File file = File.builder()
            .id(1L)
            .name(FILENAME)
            .type("txt")
            .hash(HASH)
            .size(((long) TEXT.length()))
            .data(TEXT.getBytes())
            .isDeleted(false)
            .user(user)
            .build();
    private final String token = UUID.randomUUID().toString();
    private final String NEW_FILENAME = "test2.txt";
    private final String ERROR_FILENAME = "test3.txt";

    @Test
    void getFileList() {
        final List<File> files = new ArrayList<>();
        files.add(file);
        final List<FileListDto> testFiles = new ArrayList<>(List.of(
                FileListDto.builder().filename(FILENAME).size(((long) TEXT.length())).build()
        ));

        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findFilesByUserWithLimit(1L, 1)).willReturn(files);
        given(fileListMapper.toDtos(files)).willReturn(testFiles);

        assertEquals(testFiles, fileService.getFileList(token, 1));
    }

    @Test
    void fileUpload() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.empty());
        given(fileRepository.findByHashAndUserIdAndIsDeleted(HASH, 1L, false)).willReturn(Optional.empty());
        MultipartFile body = new MockMultipartFile(FILENAME, TEXT.getBytes());

        assertEquals(file.getName(), fileService.fileUpload(token, FILENAME, body, HASH).getName());
    }

    @Test
    void fileUploadError1() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.of(file));
        MultipartFile body = new MockMultipartFile(FILENAME, TEXT.getBytes());

        assertThrows(userExceptionError400.class, () -> fileService.fileUpload(token, FILENAME, body, HASH));
    }

    @Test
    void fileUploadError2() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.empty());
        given(fileRepository.findByHashAndUserIdAndIsDeleted(HASH, 1L, false)).willReturn(Optional.of(file));
        MultipartFile body = new MockMultipartFile(FILENAME, TEXT.getBytes());

        assertThrows(userExceptionError400.class, () -> fileService.fileUpload(token, FILENAME, body, HASH));
    }

    @Test
    void fileRename() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.of(file));
        assertEquals(NEW_FILENAME, fileService.fileRename(token, FILENAME, NEW_FILENAME).getName());
    }

    @Test
    void fileRenameError() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(ERROR_FILENAME, 1L, false)).willReturn(Optional.empty());
        assertThrows(userExceptionError400.class, () -> fileService.fileRename(token, ERROR_FILENAME, NEW_FILENAME));
    }

    @Test
    void fileRemove() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.of(file));
        assertEquals(FILENAME, fileService.fileRemove(token, FILENAME).getName());
    }

    @Test
    void fileRemoveError() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(ERROR_FILENAME, 1L, false)).willReturn(Optional.empty());
        assertThrows(userExceptionError400.class, () -> fileService.fileRemove(token, ERROR_FILENAME));
    }

    @Test
    void fileDownload() {
        FileDownloadDto fileDownloadDto = FileDownloadDto
                .builder()
                .name(FILENAME)
                .type("txt")
                .size((long) TEXT.length())
                .data(TEXT.getBytes())
                .hash(HASH)
                .build();
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(FILENAME, 1L, false)).willReturn(Optional.of(file));
        given(fileDownloadMapper.toDto(file)).willReturn(fileDownloadDto);
        //assertEquals(file.getName(), fileService.fileDownload(token, FILENAME).getName());
        assertEquals(fileDownloadDto, fileService.fileDownload(token, FILENAME));
    }

    @Test
    void fileDownloadError() {
        given(jwtService.getLogin(token)).willReturn(LOGIN);
        given(userService.findByLogin(LOGIN)).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(fileRepository.findByNameAndUserIdAndIsDeleted(ERROR_FILENAME, 1L, false)).willReturn(Optional.empty());
        assertThrows(userExceptionError400.class, () -> fileService.fileDownload(token, ERROR_FILENAME));
    }
}