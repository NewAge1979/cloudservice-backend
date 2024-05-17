package pl.chrapatij.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListDto {
    @NotNull
    @Size(max = 128)
    String filename;
    @NotNull
    Long size;
}