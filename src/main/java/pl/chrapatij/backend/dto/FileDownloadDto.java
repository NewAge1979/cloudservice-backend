package pl.chrapatij.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadDto {
    @NotNull
    @Size(max = 128)
    String name;
    @NotNull
    @Size(max = 128)
    String type;
    @NotNull
    Long size;
    @NotNull
    byte[] data;
    @NotNull
    @Size(max = 256)
    String hash;
}