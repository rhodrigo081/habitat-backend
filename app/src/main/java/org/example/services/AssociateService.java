package org.example.services;

import org.example.dtos.request.AssociateRequest;
import org.example.dtos.response.AssociateResponse;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.NotFoundException;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.AssociateMapper;
import org.example.models.Associate;
import org.example.models.CaseHistory;
import org.example.models.User;
import org.example.repositories.AssociateRepository;
import org.example.repositories.CaseHistoryRepository;
import org.example.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssociateService {

    @Autowired
    private AssociateRepository associateRepository;
    @Autowired
    private AssociateMapper associateMapper;
    @Autowired
    private CaseHistoryRepository caseHistoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AssociateResponse register(AssociateRequest associateRequest, User intern) {
        if (associateRepository.existsByCpf(associateRequest.cpf())) {
            throw new InvalidCredentialsExceptions("CPF já cadastrado: " + associateRequest.cpf());
        }

        User actualIntern = intern;
        if (associateRequest.internId() != null) {
            actualIntern = userRepository.findById(associateRequest.internId())
                    .orElseThrow(() -> new NotFoundException("Estagiário" + associateRequest.internId()));
        }

        Associate associate = Associate.builder()
                .name(associateRequest.name())
                .cpf(associateRequest.cpf())
                .address(associateRequest.address())
                .phone(associateRequest.phone())
                .caseReport(associateRequest.caseReport())
                .legalGuidance(associateRequest.legalGuidance())
                .attendanceStatus(associateRequest.attendanceStatus() != null ? associateRequest.attendanceStatus() : "triagem")
                .attendanceType(associateRequest.attendanceType() != null ? associateRequest.attendanceType() : "judicial")
                .coordinatorId(associateRequest.coordinatorId())
                .intern(actualIntern)
                .build();

        associate = associateRepository.save(associate);

        CaseHistory history = CaseHistory.builder()
                .associate(associate)
                .user(actualIntern)
                .action("Caso criado")
                .build();
        caseHistoryRepository.save(history);

        return associateMapper.toResponse(associate);
    }

    @Transactional
    public AssociateResponse update(Long id, AssociateRequest associateRequest, User requester) {
        Associate associate = findWithPermission(id, requester);

        associate.setName(associateRequest.name());
        associate.setAddress(associateRequest.address());
        associate.setPhone(associateRequest.phone());
        associate.setCaseReport(associateRequest.caseReport());
        associate.setLegalGuidance(associateRequest.legalGuidance());

        String oldStatus = associate.getAttendanceStatus();
        if (associateRequest.attendanceStatus() != null) associate.setAttendanceStatus(associateRequest.attendanceStatus());
        if (associateRequest.attendanceType() != null) associate.setAttendanceType(associateRequest.attendanceType());
        if (associateRequest.coordinatorId() != null) associate.setCoordinatorId(associateRequest.coordinatorId());

        if (!associate.getCpf().equals(associateRequest.cpf())) {
            if (associateRepository.existsByCpf(associateRequest.cpf())) {
                throw new InvalidCredentialsExceptions("CPF já cadastrado: " + associateRequest.cpf());
            }
            associate.setCpf(associateRequest.cpf());
        }

        associate = associateRepository.save(associate);

        if (associateRequest.attendanceStatus() != null && !oldStatus.equals(associateRequest.attendanceStatus())) {
            CaseHistory history = CaseHistory.builder()
                    .associate(associate)
                    .user(requester)
                    .action("Status alterado para: " + associateRequest.attendanceStatus())
                    .build();
            caseHistoryRepository.save(history);
        }

        return associateMapper.toResponse(associate);
    }

    @Transactional
    public void delete(Long id, User requester) {
        Associate associate = findWithPermission(id, requester);
        associateRepository.delete(associate);
    }

    @Transactional(readOnly = true)
    public AssociateResponse findById(Long id, User requester) {
        return associateMapper.toResponse(findWithPermission(id, requester));
    }

    @Transactional(readOnly = true)
    public Page<AssociateResponse> findAll(User requester, String term, Pageable pageable) {
        if (requester.getRole() == UserRole.ADMINISTRATOR) {
            if (term != null && !term.isBlank()) {
                return associateRepository.findByTerm(term, pageable)
                        .map(associateMapper::toResponse);
            }
            return associateRepository.findAll(pageable).map(associateMapper::toResponse);
        }

        Long coordinatorId = resolveCoordinatorId(requester);

        if (term != null && !term.isBlank()) {
            return associateRepository.findByTermAndCoordinator(term, coordinatorId, pageable)
                    .map(associateMapper::toResponse);
        }
        return associateRepository.findByCoordinatorId(coordinatorId, pageable)
                .map(associateMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssociateResponse> findByIntern(Long internId, User requester) {
        if (requester.getRole() == UserRole.INTERN
                && !requester.getId().equals(internId)) {
            throw new UnauthorizedAccessException("Acesso negado");
        }
        return associateMapper.toResponseList(
                associateRepository.findByInternId(internId));
    }

    /**
     * Retorna o histórico de um associate — lógica movida do controller para cá.
     * O controller não deve depender de repositórios diretamente.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistory(Long id, User requester) {
        // Verifica permissão
        findWithPermission(id, requester);

        return caseHistoryRepository.findByAssociateIdOrderByCreatedAtDesc(id)
                .stream()
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", h.getId());
                    map.put("action", h.getAction());
                    map.put("userName", h.getUser().getName());
                    map.put("createdAt", h.getCreatedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Associate findAssociateById(Long id) {
        return associateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Associado" + id));
    }

    private Associate findWithPermission(Long id, User requester) {
        Associate associate = findAssociateById(id);
        readPermissionVerify(associate, requester);
        return associate;
    }

    private void readPermissionVerify(Associate associate, User requester) {
        if (requester.getRole() == UserRole.ADMINISTRATOR) return;

        if (requester.getRole() == UserRole.INTERN) {
            if (!associate.getIntern().getId().equals(requester.getId())) {
                throw new UnauthorizedAccessException("Acesso Negado");
            }
            return;
        }

        if (requester.getRole() == UserRole.COORDINATOR) {
            User coordinatorDoAssociate = associate.getIntern().getCoordinator();
            if (coordinatorDoAssociate == null
                    || !coordinatorDoAssociate.getId().equals(requester.getId())) {
                throw new UnauthorizedAccessException("Acesso Negado");
            }
        }
    }

    private Long resolveCoordinatorId(User requester) {
        if (requester.getRole() == UserRole.ADMINISTRATOR || requester.getRole() == UserRole.COORDINATOR) {
            return null;
        }

        if (requester.getCoordinator() == null) {
            throw new UnauthorizedAccessException("Estagiário sem coordenador vinculado.");
        }
        return requester.getCoordinator().getId();
    }
}
