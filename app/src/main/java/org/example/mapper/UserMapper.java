package org.example.mapper;

import org.example.dtos.response.UserResponse;
import org.example.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "coordinatorName", source = "coordinator", qualifiedByName = "safeCoordinatorName")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @Named("safeCoordinatorName")
    default String safeCoordinatorName(User coordinator) {
        try {
            return (coordinator != null) ? coordinator.getName() : "N/A";
        } catch (Exception e) {
            return "N/A";
        }
    }
}