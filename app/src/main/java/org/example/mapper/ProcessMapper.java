package org.example.mapper;

import org.example.dtos.response.ProcessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.example.models.Process;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProcessMapper {

    @Mapping(target = "associateId",    source = "associate.id")
    @Mapping(target = "associateName",  source = "associate.name")
    @Mapping(target = "internId",   source = "intern.id")
    @Mapping(target = "internName", source = "intern.name")
    ProcessResponse toResponse(Process process);

    List<ProcessResponse> toResponseList(List<Process> processes);
}