package org.example.services;

import org.example.dtos.request.LoginRequest;
import org.example.dtos.response.LoginResponse;
import org.example.dtos.response.UserResponse;
import org.example.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = tokenService.generateToken(user);

        String coordinatorName = (user.getCoordinator() != null) ? user.getCoordinator().getName() : "N/A";

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                coordinatorName,
                user.getStatus(),
                user.getCreatedAt()
        );

        return new LoginResponse(token, userResponse);
    }
}
