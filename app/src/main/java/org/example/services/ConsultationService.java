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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;
    @Autowired
    private AssociateService associateService;
    @Autowired
    private ConsultationMapper consultationMapper;

    @Transactional
    public ConsultationResponse register(ConsultationRequest request, User intern) {
        Associate associate = associateService.findAssociateById(request.associateId());

        Consultation consultation = Consultation.builder()
                .summary(request.summary())
                .date(request.date())
                .intern(intern)
                .associate(associate)
                .build();

        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    @Transactional
    public ConsultationResponse update(Long id, ConsultationRequest request, User requester) {
        Consultation consultation = findWithPermission(id, requester);
        Associate associate = associateService.findAssociateById(request.associateId());

        consultation.setSummary(request.summary());
        consultation.setDate(request.date());
        consultation.setAssociate(associate);

        return consultationMapper.toResponse(consultationRepository.save(consultation));
    }

    @Transactional(readOnly = true)
    public ConsultationResponse findById(Long id, User requester) {
        return consultationMapper.toResponse(findWithPermission(id, requester));
    }

    @Transactional(readOnly = true)
    public Page<ConsultationResponse> findAll(User requester, Pageable pageable) {
        if (requester.getRole() == UserRole.ADMINISTRATOR) {
            return consultationRepository.findAll(pageable).map(consultationMapper::toResponse);
        }
        if (requester.getRole() == UserRole.COORDINATOR) {
            return consultationRepository.findByCoordinatorId(requester.getId(), pageable)
                    .map(consultationMapper::toResponse);
        }
        return consultationRepository.findByInternId(requester.getId(), pageable)
                .map(consultationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConsultationResponse> findByAssociate(Long associateId, Pageable pageable) {
        return consultationRepository.findByAssociateId(associateId, pageable)
                .map(consultationMapper::toResponse);
    }

    @Transactional
    public void delete(Long id, User requester) {
        Consultation consultation = findWithPermission(id, requester);
        consultationRepository.delete(consultation);
    }

    private Consultation findWithPermission(Long id, User requester) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Consultation" + id));

        if (requester.getRole() == UserRole.ADMINISTRATOR) return consultation;

        if (requester.getRole() == UserRole.INTERN
                && !consultation.getIntern().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("Acesso não autorizado!");
        }

        if (requester.getRole() == UserRole.COORDINATOR) {
            User coordenador = consultation.getIntern().getCoordinator();
            if (coordenador == null || !coordenador.getId().equals(requester.getId())) {
                throw new UnauthorizedAccessException("Acesso não autorizado!");
            }
        }

        return consultation;
    }
}
