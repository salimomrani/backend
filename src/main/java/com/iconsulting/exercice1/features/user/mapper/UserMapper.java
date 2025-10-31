package com.iconsulting.exercice1.features.user.mapper;

import com.iconsulting.exercice1.features.user.dto.CreateUserRequest;
import com.iconsulting.exercice1.features.user.dto.UpdateUserRequest;
import com.iconsulting.exercice1.features.user.dto.UserDto;
import com.iconsulting.exercice1.features.user.entity.User;
import org.mapstruct.*;

/**
 * Mapper MapStruct pour convertir entre User et ses DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convertit une entité User en UserDto
     * Le mot de passe n'est jamais inclus dans le DTO
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserDto toDto(User user);

    /**
     * Convertit un CreateUserRequest en User
     * Le mot de passe sera hashé dans le service avant la persistance
     * Les champs d'audit et d'état sont gérés automatiquement
     */
    User toEntity(CreateUserRequest createUserRequest);

    /**
     * Met à jour une entité User avec les données d'un UpdateUserRequest
     * Ignore les champs null pour ne mettre à jour que les champs fournis
     * Ne met jamais à jour le mot de passe, l'ID ou les champs d'audit (gérés automatiquement)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "loginAttempts", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    void updateEntityFromDto(UpdateUserRequest updateUserRequest, @MappingTarget User user);
}
