package org.example.services;

import org.example.dtos.request.ConciliationRequest;
import org.example.dtos.response.ConciliationResponse;
import org.example.enums.CitationStatus;
import org.example.enums.UserRole;
import org.example.exceptions.NotFoundException;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.ConciliationMapper;
import org.example.models.Associate;
import org.example.models.Conciliation;
import org.example.models.User;
import org.example.repositories.ConciliationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConciliationService {

    @Autowired
    private ConciliationRepository conciliationRepository;
    @Autowired
    private AssociateService associateService;
    @Autowired
    private ConciliationMapper conciliationMapper;

    @Transactional
    public ConciliationResponse register(ConciliationRequest request, User intern) {
        Associate associate = associateService.findAssociateById(request.associateId());

        Conciliation conciliation = Conciliation.builder()
                .oppositePartyName(request.oppositePartyName())
                .oppositePartyContact(request.oppositePartyContact())
                .audienceDateTime(request.audienceDateTime())
                .summary(request.summary())
                .citationStatus(request.citationStatus())
                .associate(associate)
                .intern(intern)
                .build();

        return conciliationMapper.toResponse(conciliationRepository.save(conciliation));
    }

    @Transactional
    public ConciliationResponse update(Long id, ConciliationRequest request, User requester) {
        Conciliation conciliation = findWithPermission(id, requester);
        Associate associate = associateService.findAssociateById(request.associateId());

        conciliation.setOppositePartyName(request.oppositePartyName());
        conciliation.setOppositePartyContact(request.oppositePartyContact());
        conciliation.setAudienceDateTime(request.audienceDateTime());
        conciliation.setSummary(request.summary());
        conciliation.setCitationStatus(request.citationStatus());
        conciliation.setAssociate(associate);

        return conciliationMapper.toResponse(conciliationRepository.save(conciliation));
    }

    @Transactional
    public ConciliationResponse updateStatus(Long id, CitationStatus novoStatus, User requester) {
        Conciliation conciliation = findWithPermission(id, requester);
        conciliation.setCitationStatus(novoStatus);
        return conciliationMapper.toResponse(conciliationRepository.save(conciliation));
    }

    @Transactional(readOnly = true)
    public ConciliationResponse findById(Long id, User requester) {
        return conciliationMapper.toResponse(findWithPermission(id, requester));
    }

    @Transactional(readOnly = true)
    public Page<ConciliationResponse> findAll(User requester, Pageable pageable) {
        if (requester.getRole() == UserRole.ADMINISTRATOR) {
            return conciliationRepository.findAll(pageable).map(conciliationMapper::toResponse);
        }
        if (requester.getRole() ==UserRole.COORDINATOR) {
            return conciliationRepository.findByCoordinatorId(requester.getId(), pageable)
                    .map(conciliationMapper::toResponse);
        }
        return conciliationRepository.findByInternId(requester.getId())
                .stream().map(conciliationMapper::toResponse)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                ));
    }

    @Transactional(readOnly = true)
    public List<ConciliationResponse> findByAssociate(Long associateId) {
        return conciliationMapper.toResponseList(
                conciliationRepository.findByAssociateId(associateId));
    }

    @Transactional(readOnly = true)
    public List<ConciliationResponse> findNextAudiences(User requester, int daysAhead) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(daysAhead);

        if (requester.getRole() == UserRole.ADMINISTRATOR) {
            return conciliationMapper.toResponseList(
                    conciliationRepository.findAudiencesDuringThePeriod(start, end));
        }

        Long coordinatorId = requester.getRole() == UserRole.COORDINATOR
                ? requester.getId()
                : requester.getCoordinator().getId();

        return conciliationMapper.toResponseList(
                conciliationRepository.findAudienciasScheduled(coordinatorId, start, end));
    }

    @Transactional
    public void delete(Long id, User requester) {
        Conciliation conciliation = findWithPermission(id, requester);
        conciliationRepository.delete(conciliation);
    }


    private Conciliation findWithPermission(Long id, User requester) {
        Conciliation conciliation = conciliationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Conciliação" + id));

        if (requester.getRole() ==UserRole.ADMINISTRATOR) return conciliation;

        if (requester.getRole() ==UserRole.INTERN
                && !conciliation.getIntern().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("Acesso não autorizado!");
        }

        if (requester.getRole() ==UserRole.COORDINATOR) {
            User coordinator = conciliation.getIntern().getCoordinator();
            if (coordinator == null || !coordinator.getId().equals(requester.getId())) {
                throw new UnauthorizedAccessException("Acesso não autorizado!");
            }
        }

        return conciliation;
    }
}