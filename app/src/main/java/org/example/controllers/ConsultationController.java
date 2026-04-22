package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.ConsultationRequest;
import org.example.dtos.response.ConsultationResponse;
import org.example.models.User;
import org.example.services.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultations")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<ConsultationResponse> register(
            @Valid @RequestBody ConsultationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.register(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ConsultationResponse>> getAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(consultationService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(consultationService.findById(id, user));
    }

    @GetMapping("/associate/{associateId}")
    public ResponseEntity<Page<ConsultationResponse>> getByAssociate(
            @PathVariable Long associateId,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(consultationService.findByAssociate(associateId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(consultationService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        consultationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
