package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.RoleRequest;
import com.example.identity_service.dto.response.RoleResponse;
import com.example.identity_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permission", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role request);
}

