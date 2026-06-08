package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.AssociateRequest;
import org.example.dtos.response.AssociateResponse;
import org.example.models.User;
import org.example.services.AssociateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/associates")
public class AssociateController {

    @Autowired
    private AssociateService associateService;

    @PostMapping
    public ResponseEntity<AssociateResponse> register(
            @Valid @RequestBody AssociateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(associateService.register(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<AssociateResponse>> listAll(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        // repassa "search" como "term" para o service — nome interno do service não muda
        return ResponseEntity.ok(associateService.findAll(user, search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssociateResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(associateService.findById(id, user));
    }

    @GetMapping("/intern/{internId}")
    public ResponseEntity<List<AssociateResponse>> listByIntern(
            @PathVariable Long internId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(associateService.findByIntern(internId, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssociateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AssociateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(associateService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        associateService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(associateService.getHistory(id, user));
    }
}
