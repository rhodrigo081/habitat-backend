package org.example.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.enums.ProcessStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "TB_Processos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_number", nullable = false, unique = true)
    private String processNumber;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String court;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private ProcessStatus currentStatus;

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
