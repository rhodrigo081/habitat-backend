package org.example.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

public record AssociateRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Endereço é obrigatório")
        String address,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$", message = "Telefone inválido. Use o formato (99) 99999-9999")
        String phone,

        String caseReport,

        String legalGuidance,

        String attendanceStatus,

        String attendanceType,

        Long coordinatorId,

        Long internId
) {}