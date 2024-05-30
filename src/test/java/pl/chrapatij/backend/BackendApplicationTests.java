package pl.chrapatij.backend;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;
import pl.chrapatij.backend.repository.FileRepository;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class BackendApplicationTests {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String reqBodyForLogin = "{\"login\": \"admin@cloud.ru\", \"password\": \"admin\"}";
    private static final String reqBodyForLoginErr1 = "{\"login\": \"admin1@cloud.ru\", \"password\": \"admin\"}";
    private static final String reqBodyForLoginErr2 = "{\"login\": \"admin@cloud.ru\", \"password\": \"admin1\"}";
    private String authToken;
    private static final String TEXT = "File for test.";
    private final MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, TEXT.getBytes());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileRepository fileRepository;

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16.2-alpine")
    );

    @DynamicPropertySource
    private static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void clearDB() {
        fileRepository.deleteAll();
    }

    private String getAuthToken() throws Exception {
        var respToken = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(reqBodyForLogin)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode respJSON = objectMapper.readTree(respToken);
        return respJSON.get("auth-token").asText();
    }

    @Test
    void loginSuccessful() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(reqBodyForLogin)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void loginError1() throws Exception {
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(reqBodyForLoginErr1)
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("User admin1@cloud.ru not found.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void loginError2() throws Exception {
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/cloud/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(reqBodyForLoginErr2)
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("Incorrect password for user admin@cloud.ru.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void logoutSuccessful() throws Exception {
        authToken = getAuthToken();
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/cloud/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    ResultActions uploadFile() throws Exception {
        return mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .multipart(HttpMethod.POST, "/cloud/file")
                                .file(file)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", file.getOriginalFilename())
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                );
    }

    @Test
    void getFilesSuccessful() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/cloud/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("limit", "3")
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        assertEquals(file.getOriginalFilename(), objectMapper.readTree(result.getResponse().getContentAsString()).get(0).get("filename").asText());
    }

    @Test
    void getFilesError() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/cloud/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("limit", "0")
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("The limit value must be greater than 0.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void uploadFileSuccessful() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void uploadFileError1() throws Exception {
        authToken = getAuthToken();
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .multipart(HttpMethod.POST, "/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("Error input data.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void uploadFileError2() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = uploadFile().andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn().getResponse().getContentAsString();
        assertEquals("File test.txt already exists.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void renameFileSuccessful() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", file.getOriginalFilename())
                                .content(objectMapper.writeValueAsString(Map.of("filename", "test2.txt")))
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void renameFileError() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .put("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", "test1.txt")
                                .content(objectMapper.writeValueAsString(Map.of("filename", "test2.txt")))
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("File test1.txt not exists.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void removeFileSuccessful() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .delete("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", file.getOriginalFilename())
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void removeFileError() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .delete("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", "test1.txt")
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("File test1.txt not exists.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void downloadFileSuccessful() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", file.getOriginalFilename())
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(Objects.requireNonNull(file.getContentType())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        assertArrayEquals(file.getBytes(), result);
    }

    @Test
    void downloadFileError() throws Exception {
        authToken = getAuthToken();
        uploadFile().andExpect(MockMvcResultMatchers.status().isOk());
        var result = mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("filename", "test1.txt")
                                .header("auth-token", "Bearer " + authToken)
                                .cookie(new Cookie("auth-token", authToken))
                )
                .andExpect(MockMvcResultMatchers.status().is(400))
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertEquals("File test1.txt not exists.", objectMapper.readTree(result).get("message").asText());
    }

    @Test
    void testDBConnect() {
        assertTrue(postgreSQLContainer.isRunning());
    }

    @Test
    void contextLoads() {
    }
}