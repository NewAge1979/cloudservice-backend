package pl.chrapatij.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    @NotNull
    @Size(max = 128)
    String login;
    @NotNull
    @Size(max = 1024)
    String password;
}