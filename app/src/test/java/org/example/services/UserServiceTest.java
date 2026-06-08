package org.example.services;

import org.example.dtos.request.RegisterRequest;
import org.example.dtos.response.UserResponse;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.NotFoundException;
import org.example.mapper.UserMapper;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private User mockedUser;
    private UserResponse mockedUserResponse;

    @BeforeEach
    void setUp() {

        validRegisterRequest = new RegisterRequest(
                "João Silva", "joao@email.com", "Senha123!", UserRole.COORDINATOR, null
        );

        mockedUser = User.builder()
                .id(1L)
                .name("João Silva")
                .email("joao@email.com")
                .password("encodedPassword")
                .role(UserRole.COORDINATOR)
                .status(true)
                .build();

        mockedUserResponse = new UserResponse(
                1L, "João Silva", "joao@email.com", UserRole.COORDINATOR, "N/A", true, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve registrar um usuário com sucesso quando os dados forem válidos")
    void registerShouldReturnUserResponseWhenDataIsValid() {

        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockedUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(mockedUserResponse);

        UserResponse response = userService.register(validRegisterRequest);

        assertNotNull(response);
        assertEquals("joao@email.com", response.email());
        assertEquals("João Silva", response.name());

        verify(userRepository, times(1)).existsByEmail(validRegisterRequest.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsExceptions quando o e-mail já existir")
    void registerShouldThrowExceptionWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail(validRegisterRequest.email())).thenReturn(true);

        InvalidCredentialsExceptions exception = assertThrows(InvalidCredentialsExceptions.class, () -> {
            userService.register(validRegisterRequest);
        });

        assertEquals("E-mail já cadastrado: joao@email.com", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar o usuário quando buscar por um ID existente")
    void findByIdShouldReturnUserWhenIdExists() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockedUser));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("joao@email.com", result.getEmail());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando buscar por um ID inexistente")
    void findByIdShouldThrowNotFoundExceptionWhenIdDoesNotExist() {

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.findById(99L);
        });

        assertEquals("Usuário99", exception.getMessage());
    }
}