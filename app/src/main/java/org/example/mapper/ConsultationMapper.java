package org.example.mapper;

import org.example.dtos.response.ConsultationResponse;
import org.example.models.Consultation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "internId",   source = "intern.id")
    @Mapping(target = "internName", source = "intern.name")
    @Mapping(target = "associateId",    source = "associate.id")
    @Mapping(target = "associateName",  source = "associate.name")
    @Mapping(target = "createdAt", ignore = true)


    ConsultationResponse toResponse(Consultation consultation);
}
