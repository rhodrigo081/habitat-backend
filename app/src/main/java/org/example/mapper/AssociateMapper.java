package org.example.mapper;

import org.example.dtos.response.AssociateResponse;
import org.example.models.Associate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssociateMapper {

    @Mapping(target = "internId",   source = "intern.id")
    @Mapping(target = "internName", source = "intern.name")
    AssociateResponse toResponse(Associate associate);

    List<AssociateResponse> toResponseList(List<Associate> associates);
}
