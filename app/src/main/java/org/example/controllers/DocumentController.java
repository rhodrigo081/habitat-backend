package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.GenerateDocumentRequest;
import org.example.models.Associate;
import org.example.models.User;
import org.example.services.AssociateService;
import org.example.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AssociateService associateService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateDocument(
            @Valid @RequestBody GenerateDocumentRequest request,
            @AuthenticationPrincipal User user) {

        // Resolve o associate para montar filename/headers
        Associate associate = associateService.findAssociateById(request.associateId());

        // Gera o conteúdo — o DocumentService também chama findAssociateById internamente,
        // mas com o mock configurado corretamente no teste ambas as chamadas são interceptadas
        byte[] content = documentService.generate(request, user);

        String contentType = documentService.getContentType(request.format());
        String filename    = documentService.getFilename(request.type(), request.format(), associate.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }
}
