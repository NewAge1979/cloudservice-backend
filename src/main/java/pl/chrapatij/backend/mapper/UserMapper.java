package pl.chrapatij.backend.mapper;

import org.mapstruct.*;
import pl.chrapatij.backend.dto.UserDto;
import pl.chrapatij.backend.entity.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)public interface UserMapper {
    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)User partialUpdate(UserDto userDto, @MappingTarget User user);
}