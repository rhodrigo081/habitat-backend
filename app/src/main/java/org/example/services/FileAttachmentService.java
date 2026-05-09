package org.example.services;

import org.example.models.FileAttachment;
import org.example.repositories.FileAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileAttachmentService {

    @Autowired
    private FileAttachmentRepository repository;

    private final String uploadDir = "uploads/";

    public FileAttachment storeFile(MultipartFile file, String referenceId) throws IOException {
        java.io.File directory = new java.io.File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = java.util.UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
        java.nio.file.Files.write(filePath, file.getBytes());

        FileAttachment attachment = FileAttachment.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .referenceId(referenceId)
                .filePath(filePath.toString())
                .build();
        return repository.save(attachment);
    }

    public FileAttachment getFile(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Arquivo não encontrado com o id: " + id));
    }

    public List<FileAttachment> getFilesByReferenceId(String referenceId) {
        return repository.findByReferenceId(referenceId);
    }
}
