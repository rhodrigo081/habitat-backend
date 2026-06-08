package org.example.controllers;

import org.example.config.JwtAuthenticationFilter;
import org.example.models.FileAttachment;
import org.example.services.FileAttachmentService;
import org.example.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileAttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileAttachmentService fileAttachmentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @TempDir
    Path tempDir;

    private FileAttachment attachment;
    private MockMultipartFile mockFile;
    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        tempFile = tempDir.resolve("documento.pdf");
        Files.write(tempFile, "Conteudo teste".getBytes());

        attachment = FileAttachment.builder()
                .id(1L)
                .fileName("documento.pdf")
                .contentType("application/pdf")
                .referenceId("REF-123")
                .filePath(tempFile.toAbsolutePath().toString())
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "Conteudo teste".getBytes()
        );
    }

    @Test
    @DisplayName("POST /files/upload - Deve retornar 201 e dados do ficheiro")
    void uploadFileShouldReturn201() throws Exception {
        when(fileAttachmentService.storeFile(any(), any())).thenReturn(attachment);

        mockMvc.perform(multipart("/files/upload")
                .file(mockFile)
                .param("referenceId", "REF-123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fileName").value("documento.pdf"));
    }

    @Test
    @DisplayName("POST /files/upload - Deve retornar 500 em caso de erro")
    void uploadFileShouldReturn500OnError() throws Exception {
        when(fileAttachmentService.storeFile(any(), any())).thenThrow(new RuntimeException("Erro interno"));

        mockMvc.perform(multipart("/files/upload")
                .file(mockFile)
                .param("referenceId", "REF-123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /files/{id} - Deve retornar 200 e o ficheiro")
    void getFileShouldReturn200AndFile() throws Exception {
        when(fileAttachmentService.getFile(any())).thenReturn(attachment);

        mockMvc.perform(get("/files/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documento.pdf\""))
                .andExpect(content().bytes("Conteudo teste".getBytes()));
    }

    @Test
    @DisplayName("GET /files/reference/{referenceId} - Deve retornar 200 e lista")
    void getFilesByReferenceIdShouldReturn200() throws Exception {
        when(fileAttachmentService.getFilesByReferenceId(any())).thenReturn(List.of(attachment));

        mockMvc.perform(get("/files/reference/REF-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}