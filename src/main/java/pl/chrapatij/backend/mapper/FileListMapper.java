package pl.chrapatij.backend.mapper;

import org.mapstruct.*;
import pl.chrapatij.backend.dto.FileListDto;
import pl.chrapatij.backend.entity.File;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileListMapper {
    @Mapping(source = "filename", target = "name")
    File toEntity(FileListDto fileDto);

    @Mapping(source = "name", target = "filename")
    FileListDto toDto(File file);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<FileListDto> toDtos(List<File> files);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    File partialUpdate(FileListDto fileDto, @MappingTarget File file);
}