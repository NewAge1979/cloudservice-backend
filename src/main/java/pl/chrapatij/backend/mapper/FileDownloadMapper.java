package pl.chrapatij.backend.mapper;

import org.mapstruct.*;
import pl.chrapatij.backend.dto.FileDownloadDto;
import pl.chrapatij.backend.entity.File;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileDownloadMapper {
    File toEntity(FileDownloadDto fileDownloadDto);

    FileDownloadDto toDto(File file);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    File partialUpdate(FileDownloadDto fileDownloadDto, @MappingTarget File file);
}