package org.example.services;

import org.example.models.User;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve retornar UserDetails quando o email existir")
    void loadUserByUsernameShouldReturnUserDetailsWhenEmailExists() {
        String email = "test@email.com";
        User mockedUser = User.builder().email(email).password("senha123").build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockedUser));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o email não existir")
    void loadUserByUsernameShouldThrowExceptionWhenEmailDoesNotExist() {
        String email = "naoexiste@email.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(email);
        });

        assertEquals("Usuário não encontrado com email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }
}