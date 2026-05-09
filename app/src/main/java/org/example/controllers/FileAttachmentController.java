package org.example.controllers;

import org.example.models.FileAttachment;
import org.example.services.FileAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileAttachmentController {

    @Autowired
    private FileAttachmentService fileAttachmentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("referenceId") String referenceId) {
        try {
            FileAttachment attachment = fileAttachmentService.storeFile(file, referenceId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", attachment.getId());
            response.put("fileName", attachment.getFileName());
            response.put("contentType", attachment.getContentType());
            response.put("referenceId", attachment.getReferenceId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao realizar o upload do arquivo: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        try {
            FileAttachment attachment = fileAttachmentService.getFile(id);
            java.nio.file.Path path = java.nio.file.Paths.get(attachment.getFilePath());
            byte[] data = java.nio.file.Files.readAllBytes(path);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .body(data);
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<List<Map<String, Object>>> getFilesByReferenceId(@PathVariable String referenceId) {
        List<FileAttachment> files = fileAttachmentService.getFilesByReferenceId(referenceId);
        List<Map<String, Object>> response = files.stream().map(f -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", f.getId());
            map.put("fileName", f.getFileName());
            map.put("contentType", f.getContentType());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
