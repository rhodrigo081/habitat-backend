package org.example.services;

import org.example.models.FileAttachment;
import org.example.repositories.FileAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileAttachmentServiceTest {

    @Mock
    private FileAttachmentRepository repository;

    @InjectMocks
    private FileAttachmentService fileAttachmentService;

    @TempDir
    Path tempDir;

    private FileAttachment attachment;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileAttachmentService, "uploadDir", tempDir.toAbsolutePath().toString() + "/");

        attachment = FileAttachment.builder()
                .id(1L)
                .fileName("documento.pdf")
                .contentType("application/pdf")
                .referenceId("REF-123")
                .filePath(tempDir.toAbsolutePath().toString() + "/mock_documento.pdf")
                .build();

        multipartFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "Conteúdo simulado do arquivo".getBytes()
        );
    }

    @Test
    @DisplayName("Deve processar o ficheiro e registar na base de dados")
    void storeFileShouldSaveToDiskAndDatabase() throws IOException {
        when(repository.save(any(FileAttachment.class))).thenReturn(attachment);

        FileAttachment result = fileAttachmentService.storeFile(multipartFile, "REF-123");

        assertNotNull(result);
        assertEquals("documento.pdf", result.getFileName());
        assertEquals("REF-123", result.getReferenceId());
        
        verify(repository, times(1)).save(any(FileAttachment.class));
    }

    @Test
    @DisplayName("Deve retornar o ficheiro quando o ID existir")
    void getFileShouldReturnFileWhenIdExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(attachment));

        FileAttachment result = fileAttachmentService.getFile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("documento.pdf", result.getFileName());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando o ID não existir")
    void getFileShouldThrowExceptionWhenIdDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileAttachmentService.getFile(99L);
        });

        assertEquals("Arquivo não encontrado com o id: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar lista de ficheiros pelo referenceId")
    void getFilesByReferenceIdShouldReturnList() {
        when(repository.findByReferenceId("REF-123")).thenReturn(List.of(attachment));

        List<FileAttachment> result = fileAttachmentService.getFilesByReferenceId("REF-123");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REF-123", result.get(0).getReferenceId());
    }
}