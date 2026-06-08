package org.example.services;

import org.example.dtos.request.ConsultationRequest;
import org.example.dtos.response.ConsultationResponse;
import org.example.enums.UserRole;
import org.example.exceptions.NotFoundException;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.ConsultationMapper;
import org.example.models.Associate;
import org.example.models.Consultation;
import org.example.models.User;
import org.example.repositories.ConsultationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AssociateService associateService;

    @Mock
    private ConsultationMapper consultationMapper;

    @InjectMocks
    private ConsultationService consultationService;

    private ConsultationRequest request;
    private User internUser;
    private User adminUser;
    private Associate associate;
    private Consultation consultation;
    private ConsultationResponse responseDto;

    @BeforeEach
    void setUp() {
        request = new ConsultationRequest("Resumo da consulta", LocalDate.now(), 1L);
        
        internUser = User.builder().id(2L).role(UserRole.INTERN).name("Estagiario").build();
        adminUser = User.builder().id(3L).role(UserRole.ADMINISTRATOR).build();
        
        associate = Associate.builder().id(1L).name("Maria Souza").build();
        
        consultation = Consultation.builder()
                .id(1L)
                .summary("Resumo da consulta")
                .date(LocalDate.now())
                .intern(internUser)
                .associate(associate)
                .build();
                
        responseDto = new ConsultationResponse(
                1L, "Resumo da consulta", LocalDate.now(), 1L, "Maria Souza", 2L, "Estagiario", LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve registar consulta com sucesso")
    void registerShouldSaveConsultation() {
        when(associateService.findAssociateById(request.associateId())).thenReturn(associate);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);
        when(consultationMapper.toResponse(any(Consultation.class))).thenReturn(responseDto);

        ConsultationResponse result = consultationService.register(request, internUser);

        assertNotNull(result);
        assertEquals("Resumo da consulta", result.summary());
        verify(associateService, times(1)).findAssociateById(request.associateId());
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Deve retornar consulta por ID quando o utilizador tiver permissão")
    void findByIdShouldReturnConsultationWhenUserHasPermission() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationMapper.toResponse(consultation)).thenReturn(responseDto);

        ConsultationResponse result = consultationService.findById(1L, adminUser);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(consultationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedAccessException quando um estagiário aceder à consulta de outro")
    void findByIdShouldThrowUnauthorizedAccessExceptionWhenInternAccessesOthersConsultation() {
        User anotherIntern = User.builder().id(4L).role(UserRole.INTERN).build();
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        assertThrows(UnauthorizedAccessException.class, () -> {
            consultationService.findById(1L, anotherIntern);
        });
    }
    
    @Test
    @DisplayName("Deve remover consulta com sucesso")
    void deleteShouldRemoveConsultation() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        
        consultationService.delete(1L, adminUser);
        
        verify(consultationRepository, times(1)).delete(consultation);
    }
    
    @Test
    @DisplayName("Deve lançar NotFoundException quando a consulta não existir")
    void findByIdShouldThrowNotFoundExceptionWhenConsultationDoesNotExist() {
        when(consultationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            consultationService.findById(99L, adminUser);
        });
    }
}