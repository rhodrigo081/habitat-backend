package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.ConciliationRequest;
import org.example.dtos.response.ConciliationResponse;
import org.example.enums.CitationStatus;
import org.example.models.User;
import org.example.services.ConciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conciliations")
public class ConciliationController {

    @Autowired
    private ConciliationService conciliationService;

    @PostMapping
    public ResponseEntity<ConciliationResponse> register(
            @Valid @RequestBody ConciliationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conciliationService.register(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ConciliationResponse>> getAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "audienceDateTime") Pageable pageable) {
        return ResponseEntity.ok(conciliationService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConciliationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(conciliationService.findById(id, user));
    }

    @GetMapping("/associate/{associateId}")
    public ResponseEntity<List<ConciliationResponse>> getByAssociate(
            @PathVariable Long associateId) {
        return ResponseEntity.ok(conciliationService.findByAssociate(associateId));
    }

    @GetMapping("/next-audiences")
    public ResponseEntity<List<ConciliationResponse>> getNexAudiences(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int dias) {
        return ResponseEntity.ok(conciliationService.findNextAudiences(user, dias));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConciliationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ConciliationRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(conciliationService.update(id, request, user));
    }

    @PatchMapping("/{id}/citation-status")
    public ResponseEntity<ConciliationResponse> updateCitationStatus(
            @PathVariable Long id,
            @RequestParam CitationStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(conciliationService.updateStatus(id, status, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        conciliationService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
