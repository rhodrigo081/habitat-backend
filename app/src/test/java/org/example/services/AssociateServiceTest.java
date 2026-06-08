package org.example.services;

import org.example.dtos.request.AssociateRequest;
import org.example.dtos.response.AssociateResponse;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.NotFoundException;
import org.example.mapper.AssociateMapper;
import org.example.models.Associate;
import org.example.models.CaseHistory;
import org.example.models.User;
import org.example.repositories.AssociateRepository;
import org.example.repositories.CaseHistoryRepository;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class AssociateServiceTest {

    @Mock
    private AssociateRepository associateRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssociateMapper associateMapper;

    @InjectMocks
    private AssociateService associateService;

    private AssociateRequest validRequest;
    private User internUser;
    private User adminUser;
    private Associate associate;
    private AssociateResponse responseDto;

    @BeforeEach
    void setUp() {
        validRequest = new AssociateRequest(
                "Maria Souza", "12345678900", "Rua A", "(81) 99999-9999",
                "Relato", "Orientação", "triagem", "judicial", 1L, null
        );

        internUser = User.builder().id(2L).role(UserRole.INTERN).name("Estagiario").build();
        adminUser = User.builder().id(3L).role(UserRole.ADMINISTRATOR).build();

        associate = Associate.builder()
                .id(1L).name("Maria Souza").cpf("12345678900")
                .intern(internUser).build();

        responseDto = new AssociateResponse(
                1L, "Maria Souza", "12345678900", "Rua A", "(81) 99999-9999",
                "Relato", "Orientação", "triagem", "judicial", 1L, 2L, "Estagiario",
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve registar associado com sucesso e guardar histórico")
    void registerShouldReturnResponseAndSaveHistoryWhenSuccessful() {
        when(associateRepository.existsByCpf(validRequest.cpf())).thenReturn(false);
        when(associateRepository.save(any(Associate.class))).thenReturn(associate);
        when(associateMapper.toResponse(any(Associate.class))).thenReturn(responseDto);

        AssociateResponse result = associateService.register(validRequest, internUser);

        assertNotNull(result);
        assertEquals("Maria Souza", result.name());
        assertEquals("12345678900", result.cpf());
        
        verify(associateRepository, times(1)).save(any(Associate.class));
        verify(caseHistoryRepository, times(1)).save(any(CaseHistory.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registar associado com CPF já existente")
    void registerShouldThrowExceptionWhenCpfExists() {
        when(associateRepository.existsByCpf(validRequest.cpf())).thenReturn(true);

        InvalidCredentialsExceptions exception = assertThrows(InvalidCredentialsExceptions.class, () -> {
            associateService.register(validRequest, internUser);
        });

        assertEquals("CPF já cadastrado: 12345678900", exception.getMessage());
        verify(associateRepository, never()).save(any(Associate.class));
        verify(caseHistoryRepository, never()).save(any(CaseHistory.class));
    }

    @Test
    @DisplayName("Deve retornar associado por ID quando o utilizador tiver permissão")
    void findByIdShouldReturnAssociateWhenUserHasPermission() {
        when(associateRepository.findById(1L)).thenReturn(Optional.of(associate));
        when(associateMapper.toResponse(associate)).thenReturn(responseDto);

        AssociateResponse result = associateService.findById(1L, adminUser);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando o associado não for encontrado por ID")
    void findByIdShouldThrowNotFoundExceptionWhenAssociateDoesNotExist() {
        when(associateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            associateService.findById(99L, adminUser);
        });
    }
}