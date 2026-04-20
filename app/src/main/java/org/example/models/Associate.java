package org.example.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "associados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Associate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "relato_caso", columnDefinition = "TEXT")
    private String caseReport;

    @Column(name = "orientacao_juridica", columnDefinition = "TEXT")
    private String legalGuidance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estagiario_id", nullable = false)
    private User intern;

    @OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Service> service;

    @OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Process> processes;

    @OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Conciliation> conciliations;

    @Column(name = "criado_em")
    private LocalDateTime createdAt;

    @Column(name = "atualizado_em")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}