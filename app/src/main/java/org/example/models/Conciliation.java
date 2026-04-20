package org.example.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.CitationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "conciliacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parte_contraria_nome", nullable = false)
    private String oppositePartyName;

    @Column(name = "parte_contraria_contato")
    private String oppositePartyContact;

    @Column(name = "data_hora_audiencia")
    private LocalDateTime audienceDateTime;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_citacao", nullable = false)
    private CitationStatus citationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associado_id", nullable = false)
    private Associate associate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estagiario_id", nullable = false)
    private User intern;

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

