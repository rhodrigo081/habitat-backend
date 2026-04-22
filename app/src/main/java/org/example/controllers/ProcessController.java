package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.request.ProcessRequest;
import org.example.dtos.response.ProcessResponse;
import org.example.enums.ProcessStatus;
import org.example.models.User;
import org.example.services.ProcessService;
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
@RequestMapping("/processes")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    @PostMapping
    public ResponseEntity<ProcessResponse> register(
            @Valid @RequestBody ProcessRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(processService.register(request, user));
    }

    @GetMapping
    public ResponseEntity<Page<ProcessResponse>> getAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAT") Pageable pageable) {
        return ResponseEntity.ok(processService.findAll(user, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(processService.findById(id, user));
    }

    @GetMapping("/associate/{associateId}")
    public ResponseEntity<List<ProcessResponse>> getByAssociate(
            @PathVariable Long associateId) {
        return ResponseEntity.ok(processService.findByAssociate(associateId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProcessRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(processService.update(id, request, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProcessResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ProcessStatus status,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(processService.updateStatus(id, status, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        processService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
