package org.example.services;

import org.example.dtos.request.ProcessRequest;
import org.example.dtos.response.ProcessResponse;
import org.example.enums.ProcessStatus;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.ProcessMapper;
import org.example.models.Associate;
import org.example.models.Process;
import org.example.models.User;
import org.example.repositories.ProcessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private AssociateService associateService;

    @Mock
    private ProcessMapper processMapper;

    @InjectMocks
    private ProcessService processService;

    private User admin;
    private User coordinator;
    private User intern;
    private User unauthorizedIntern;
    private Associate associate;
    private Process process;
    private ProcessRequest processRequest;
    private ProcessResponse processResponse;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).role(UserRole.ADMINISTRATOR).build();
        coordinator = User.builder().id(2L).role(UserRole.COORDINATOR).build();
        
        intern = User.builder().id(3L).role(UserRole.INTERN).coordinator(coordinator).build();
        unauthorizedIntern = User.builder().id(4L).role(UserRole.INTERN).build(); // Estagiário de outro processo

        associate = Associate.builder().id(10L).name("João Silva").build();

        processRequest = new ProcessRequest(
                "0001234-56.2023.8.26.0000",
                "São Paulo",
                "1ª Vara Cível",
                "Ação de Indenização",
                ProcessStatus.INITIAL,
                associate.getId()
        );

        process = Process.builder()
                .id(100L)
                .processNumber(processRequest.processNumber())
                .city(processRequest.city())
                .court(processRequest.court())
                .description(processRequest.description())
                .currentStatus(processRequest.status())
                .associate(associate)
                .intern(intern)
                .build();

        processResponse = new ProcessResponse(
                process.getId(),
                process.getProcessNumber(),
                process.getCity(),
                process.getCourt(),
                process.getDescription(),
                process.getCurrentStatus(),
                associate.getId(),
                associate.getName(),
                intern.getId(),
                "Nome Estagiário",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve registar um processo com sucesso")
    void registerShouldSaveProcessSuccessfully() {
        when(processRepository.existsByProcessNumber(processRequest.processNumber())).thenReturn(false);
        when(associateService.findAssociateById(processRequest.associateId())).thenReturn(associate);
        when(processRepository.save(any(Process.class))).thenReturn(process);
        when(processMapper.toResponse(any(Process.class))).thenReturn(processResponse);

        ProcessResponse result = processService.register(processRequest, intern);

        assertNotNull(result);
        assertEquals(processRequest.processNumber(), result.processNumber());
        verify(processRepository, times(1)).save(any(Process.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registar processo com número já existente")
    void registerShouldThrowWhenProcessNumberExists() {
        when(processRepository.existsByProcessNumber(processRequest.processNumber())).thenReturn(true);

        InvalidCredentialsExceptions exception = assertThrows(InvalidCredentialsExceptions.class, () -> {
            processService.register(processRequest, intern);
        });

        assertTrue(exception.getMessage().contains("já cadastrado"));
        verify(processRepository, never()).save(any(Process.class));
    }

    @Test
    @DisplayName("Deve atualizar um processo com sucesso")
    void updateShouldUpdateProcessSuccessfully() {
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        when(associateService.findAssociateById(processRequest.associateId())).thenReturn(associate);
        when(processRepository.save(any(Process.class))).thenReturn(process);
        when(processMapper.toResponse(any(Process.class))).thenReturn(processResponse);

        ProcessResponse result = processService.update(process.getId(), processRequest, intern);

        assertNotNull(result);
        verify(processRepository, times(1)).save(any(Process.class));
    }

    @Test
    @DisplayName("Deve lançar UnauthorizedAccessException se estagiário tentar acessar processo de outro")
    void findByIdShouldThrowForUnauthorizedIntern() {
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));

        assertThrows(UnauthorizedAccessException.class, () -> {
            processService.findById(process.getId(), unauthorizedIntern);
        });
    }

    @Test
    @DisplayName("Deve permitir Administrador acessar qualquer processo")
    void findByIdShouldReturnProcessForAdministrator() {
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        when(processMapper.toResponse(process)).thenReturn(processResponse);

        ProcessResponse result = processService.findById(process.getId(), admin);

        assertNotNull(result);
        assertEquals(process.getId(), result.id());
    }

    @Test
    @DisplayName("Deve atualizar o status do processo com sucesso")
    void updateStatusShouldUpdateSuccessfully() {
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));
        when(processRepository.save(any(Process.class))).thenReturn(process);
        when(processMapper.toResponse(any(Process.class))).thenReturn(processResponse);

        ProcessResponse result = processService.updateStatus(process.getId(), ProcessStatus.JUDGMENT, intern);

        assertNotNull(result);
        verify(processRepository, times(1)).save(any(Process.class));
    }

    @Test
    @DisplayName("Deve listar todos os processos para Administrador")
    void findAllShouldReturnAllForAdministrator() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Process> page = new PageImpl<>(List.of(process));
        
        when(processRepository.findAll(pageable)).thenReturn(page);
        when(processMapper.toResponse(any(Process.class))).thenReturn(processResponse);

        Page<ProcessResponse> result = processService.findAll(admin, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(processRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve deletar processo com sucesso")
    void deleteShouldRemoveProcess() {
        when(processRepository.findById(process.getId())).thenReturn(Optional.of(process));

        processService.delete(process.getId(), admin);

        verify(processRepository, times(1)).delete(process);
    }
}