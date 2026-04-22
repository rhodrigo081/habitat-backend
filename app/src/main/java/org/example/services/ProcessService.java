package org.example.services;


import org.example.dtos.request.ProcessRequest;
import org.example.dtos.response.ProcessResponse;
import org.example.enums.ProcessStatus;
import org.example.enums.UserRole;
import org.example.exceptions.InvalidArgumentException;
import org.example.exceptions.InvalidCredentialsExceptions;
import org.example.exceptions.NotFoundException;
import org.example.exceptions.UnauthorizedAccessException;
import org.example.mapper.ProcessMapper;
import org.example.models.Associate;
import org.example.models.User;
import org.example.repositories.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.models.Process;

import java.util.List;

@Service
public class ProcessService {

    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private AssociateService associateService;
    @Autowired
    private ProcessMapper processMapper;

    @Transactional
    public ProcessResponse register(ProcessRequest request, User intern) {
        if (processRepository.existsByProcessNumber(request.processNumber())) {
            throw new InvalidCredentialsExceptions("Número de process já cadastrado: " + request.processNumber());
        }
        Associate associate = associateService.findAssociateById(request.associateId());

       Process process = Process.builder()
                .processNumber(request.processNumber())
                .city(request.city())
                .court(request.court())
                .description(request.description())
                .currentStatus(request.status())
                .associate(associate)
                .intern(intern)
                .build();

        return processMapper.toResponse(processRepository.save(process));
    }

    @Transactional
    public ProcessResponse update(Long id, ProcessRequest request, User requester) {
        Process process = findWithPermission(id, requester);

        if (!process.getProcessNumber().equals(request.processNumber())
                && processRepository.existsByProcessNumber(request.processNumber())) {
            throw new InvalidArgumentException("Número de process já cadastrado: " + request.processNumber());
        }

        Associate associate = associateService.findAssociateById(request.associateId());

        process.setProcessNumber(request.processNumber());
        process.setCity(request.city());
        process.setCourt(request.court());
        process.setDescription(request.description());
        process.setCurrentStatus(request.status());
        process.setAssociate(associate);

        return processMapper.toResponse(processRepository.save(process));
    }

    @Transactional
    public ProcessResponse updateStatus(Long id, ProcessStatus newStatus, User requester) {
        Process process = findWithPermission(id, requester);
        process.setCurrentStatus(newStatus);
        return processMapper.toResponse(processRepository.save(process));
    }

    @Transactional(readOnly = true)
    public ProcessResponse findById(Long id, User requester) {
        return processMapper.toResponse(findWithPermission(id, requester));
    }

    @Transactional(readOnly = true)
    public Page<ProcessResponse> findAll(User requester, Pageable pageable) {
        if (requester.getRole() == UserRole.ADMINISTRATOR) {
            return processRepository.findAll(pageable).map(processMapper::toResponse);
        }
        if (requester.getRole() == UserRole.COORDINATOR) {
            return processRepository.findByCoordinatorId(requester.getId(), pageable)
                    .map(processMapper::toResponse);
        }
        return processRepository.findByInternId(requester.getId())
                .stream().map(processMapper::toResponse)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                ));
    }

    @Transactional(readOnly = true)
    public List<ProcessResponse> findByAssociate(Long associateId) {
        return processMapper.toResponseList(processRepository.findByAssociateId(associateId));
    }

    @Transactional
    public void delete(Long id, User requester) {
        Process process = findWithPermission(id, requester);
        processRepository.delete(process);
    }

    public Process findEntityById(Long id) {
        return processRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Process" + id));
    }

    private Process findWithPermission(Long id, User requester) {
        Process process = findEntityById(id);

        if (requester.getRole() == UserRole.ADMINISTRATOR) return process;

        if (requester.getRole() == UserRole.INTERN
                && !process.getIntern().getId().equals(requester.getId())) {
            throw new UnauthorizedAccessException("Acesso não autorizado!");
        }

        if (requester.getRole() == UserRole.COORDINATOR) {
            User coordinator = process.getIntern().getCoordinator();
            if (coordinator == null || !coordinator.getId().equals(requester.getId())) {
                throw new UnauthorizedAccessException("Acesso não autorizado!");
            }
        }

        return process;
    }
}
