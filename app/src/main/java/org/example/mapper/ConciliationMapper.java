package org.example.mapper;

import org.example.dtos.response.ConciliationResponse;
import org.example.models.Conciliation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConciliationMapper {

    @Mapping(target = "associateId",    source = "associate.id")
    @Mapping(target = "associateName",  source = "associate.name")
    @Mapping(target = "internId",   source = "intern.id")
    @Mapping(target = "internName", source = "intern.name")
    ConciliationResponse toResponse(Conciliation conciliation);

    List<ConciliationResponse> toResponseList(List<Conciliation> conciliations);
}