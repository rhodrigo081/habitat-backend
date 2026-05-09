package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.AssociateRequest;
import org.example.dtos.response.AssociateResponse;
import org.example.models.User;
import org.example.services.AssociateService;
import org.example.repositories.CaseHistoryRepository;
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
@RequestMapping("/associates")
public class AssociateController {

    @Autowired
    private AssociateService associateService;
    
    @Autowired
    private CaseHistoryRepository caseHistoryRepository;

    @PostMapping
    public ResponseEntity<AssociateResponse> register(
            @Valid @RequestBody AssociateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(associateService.register(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<AssociateResponse>> listALl(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String term,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(associateService.findAll(user, term, pageable));
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

    @GetMapping("/{id}/history")
    public ResponseEntity<List<java.util.Map<String, Object>>> getHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        // Verifica permissão (vai jogar exception se não puder ver)
        associateService.findById(id, user);

        var history = caseHistoryRepository.findByAssociateIdOrderByCreatedAtDesc(id);
        var response = history.stream().map(h -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", h.getId());
            map.put("action", h.getAction());
            map.put("userName", h.getUser().getName());
            map.put("createdAt", h.getCreatedAt());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }
}