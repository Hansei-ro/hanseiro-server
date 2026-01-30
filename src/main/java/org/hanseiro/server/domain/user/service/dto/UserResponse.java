package org.hanseiro.server.domain.user.service.dto;

import org.hanseiro.server.domain.user.model.UserEntity;

public record UserResponse(
        Long id,
        String email,
        String name,
        String department,
        Boolean isNew
) {
    public static UserResponse of(UserEntity user, boolean isNew) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getDepartment(),
                isNew
        );
    }
}