package org.example.services;

import org.example.dtos.request.RegisterRequest;
import org.example.dtos.request.UserUpdateRequest;
import org.example.dtos.response.UserResponse;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.NotFoundException;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.UserMapper;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new InvalidCredentialsExceptions("E-mail já cadastrado: " + request.email());
        }

        if (request.role() == UserRole.ADMINISTRATOR) {
            throw new InvalidCredentialsExceptions("Não é permitido cadastrar novos administradores pelo sistema.");
        }

        User.UserBuilder builder = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(true);

        if (request.role() == UserRole.INTERN) {
            if (request.coordinatorId() == null) {
                throw new InvalidCredentialsExceptions("Estagiário deve ser vinculado a um coordenador.");
            }
            User coordinator = userRepository.findById(request.coordinatorId())
                    .orElseThrow(() -> new UnauthorizedAccessException("Coordenador" + request.coordinatorId()));
            if (coordinator.getRole() != UserRole.COORDINATOR) {
                throw new InvalidCredentialsExceptions("O usuário informado não é um coordenador.");
            }
            builder.coordinator(coordinator);
        }

        return userMapper.toResponse(userRepository.save(builder.build()));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findById(id);

        if (request.name() != null)        user.setName(request.name());
        if (request.status() != null)       user.setStatus(request.status());
        if (request.password() != null)       user.setPassword(passwordEncoder.encode(request.password()));
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new InvalidCredentialsExceptions("E-mail já em uso: " + request.email());
            }
            user.setEmail(request.email());
        }
        if (request.coordinatorId() != null && user.getRole() == UserRole.INTERN) {
            User coordinator = userRepository.findById(request.coordinatorId())
                    .orElseThrow(() -> new NotFoundException("Coordenador" + request.coordinatorId()));
            user.setCoordinator(coordinator);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void inactive(Long id) {
        User user = findById(id);
        user.setStatus(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findByIdResponse(Long id) {
        return userMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userMapper.toResponseList(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findByUserRole(UserRole role) {
        return userMapper.toResponseList(userRepository.findByRoleAndStatusTrue(role));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findInternByCoordinator(Long coordinatorId) {
        return userMapper.toResponseList(
                userRepository.findInternsByCoordinatorId(coordinatorId));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário" + id));
    }

    public void coordinatorAccessValidation(User requester, Long targetCoordinatorId) {
        if (requester.getRole() == UserRole.COORDINATOR
                && !requester.getId().equals(targetCoordinatorId)) {
            throw new UnauthorizedAccessException(
                    "Coordenador só pode visualizar dados vinculados ao seu próprio ID.");
        }
    }
}
