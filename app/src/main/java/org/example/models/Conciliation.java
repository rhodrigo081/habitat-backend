package org.example.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.CitationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_Conciliations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opposite_party_name", nullable = false)
    private String oppositePartyName;

    @Column(name = "opposite_party_contact")
    private String oppositePartyContact;

    @Column(name = "audience_date_time")
    private LocalDateTime audienceDateTime;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "citation_status", nullable = false)
    private CitationStatus citationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associate_id", nullable = false)
    private Associate associate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intern_id", nullable = false)
    private User intern;

    @Column(name = "created_At")
    private LocalDateTime createdAt;

    @Column(name = "updated_At")
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

