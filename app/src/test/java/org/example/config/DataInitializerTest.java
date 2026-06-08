package org.example.config;

import org.example.enums.UserRole;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @InjectMocks
    private DataInitializer dataInitializer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final String adminEmail = "admin@habitat.com";
    private final String adminPassword = "secretPassword";
    private final String adminName = "Admin Master";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", adminEmail);
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", adminPassword);
        ReflectionTestUtils.setField(dataInitializer, "adminName", adminName);
    }

    @Test
    @DisplayName("Deve criar o usuário administrador quando ele não existir no banco")
    void shouldCreateAdminUserWhenItDoesNotExist() {
        when(userRepository.existsByEmail(adminEmail)).thenReturn(false);
        when(passwordEncoder.encode(adminPassword)).thenReturn("encodedPassword");

        dataInitializer.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(adminEmail);
        assertThat(savedUser.getName()).isEqualTo(adminName);
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMINISTRATOR);
        assertThat(savedUser.getStatus()).isTrue();
    }

    @Test
    @DisplayName("Não deve criar o usuário administrador se ele já existir no banco")
    void shouldNotCreateAdminUserWhenItAlreadyExists() {
        when(userRepository.existsByEmail(adminEmail)).thenReturn(true);

        dataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}