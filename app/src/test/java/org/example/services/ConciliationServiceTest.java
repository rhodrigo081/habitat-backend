package org.example.services;

import org.example.dtos.request.ConciliationRequest;
import org.example.dtos.response.ConciliationResponse;
import org.example.enums.CitationStatus;
import org.example.enums.UserRole;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.ConciliationMapper;
import org.example.models.Associate;
import org.example.models.Conciliation;
import org.example.models.User;
import org.example.repositories.ConciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConciliationServiceTest {

    @Mock
    private ConciliationRepository conciliationRepository;

    @Mock
    private AssociateService associateService;

    @Mock
    private ConciliationMapper conciliationMapper;

    @InjectMocks
    private ConciliationService conciliationService;

    private ConciliationRequest request;
    private User internUser;
    private User adminUser;
    private Associate associate;
    private Conciliation conciliation;
    private ConciliationResponse responseDto;

    @BeforeEach
    void setUp() {
        request = new ConciliationRequest(
                "Parte Contrária", "99999999", LocalDateTime.now().plusDays(5),
                "Resumo", CitationStatus.PENDING, 1L
        );
        internUser = User.builder().id(2L).role(UserRole.INTERN).name("Estagiario").build();
        adminUser = User.builder().id(3L).role(UserRole.ADMINISTRATOR).build();
        associate = Associate.builder().id(1L).name("Maria Souza").build();
        conciliation = Conciliation.builder().id(1L).oppositePartyName("Parte Contrária")
                .citationStatus(CitationStatus.PENDING).intern(internUser).associate(associate).build();
        responseDto = new ConciliationResponse(
                1L, "Parte Contrária", "99999999", LocalDateTime.now().plusDays(5),
                "Resumo", CitationStatus.PENDING, 1L, "Maria Souza", 2L, "Estagiario", LocalDateTime.now()
        );
    }

    @Test
    void registerShouldSaveConciliation() {
        when(associateService.findAssociateById(request.associateId())).thenReturn(associate);
        when(conciliationRepository.save(any(Conciliation.class))).thenReturn(conciliation);
        when(conciliationMapper.toResponse(any(Conciliation.class))).thenReturn(responseDto);

        ConciliationResponse result = conciliationService.register(request, internUser);

        assertNotNull(result);
        assertEquals("Parte Contrária", result.oppositePartyName());
        verify(associateService, times(1)).findAssociateById(request.associateId());
        verify(conciliationRepository, times(1)).save(any(Conciliation.class));
    }

    @Test
    void findByIdShouldReturnConciliationWhenUserHasPermission() {
        when(conciliationRepository.findById(1L)).thenReturn(Optional.of(conciliation));
        when(conciliationMapper.toResponse(conciliation)).thenReturn(responseDto);

        ConciliationResponse result = conciliationService.findById(1L, adminUser);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(conciliationRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdShouldThrowUnauthorizedAccessExceptionWhenInternAccessesOthersConciliation() {
        User anotherIntern = User.builder().id(4L).role(UserRole.INTERN).build();
        when(conciliationRepository.findById(1L)).thenReturn(Optional.of(conciliation));

        assertThrows(UnauthorizedAccessException.class, () -> {
            conciliationService.findById(1L, anotherIntern);
        });
    }

    @Test
    void updateStatusShouldUpdateAndReturnConciliation() {
        when(conciliationRepository.findById(1L)).thenReturn(Optional.of(conciliation));
        when(conciliationRepository.save(any(Conciliation.class))).thenReturn(conciliation);
        when(conciliationMapper.toResponse(any(Conciliation.class))).thenReturn(responseDto);

        ConciliationResponse result = conciliationService.updateStatus(1L, CitationStatus.CITED, adminUser);

        assertNotNull(result);
        assertEquals(CitationStatus.CITED, conciliation.getCitationStatus());
        verify(conciliationRepository, times(1)).save(conciliation);
    }
    
    @Test
    void deleteShouldRemoveConciliation() {
        when(conciliationRepository.findById(1L)).thenReturn(Optional.of(conciliation));
        
        conciliationService.delete(1L, adminUser);
        
        verify(conciliationRepository, times(1)).delete(conciliation);
    }
}