package club.sportsapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MemberUpdateDTO(

        @NotNull
        UUID uuid,

        @NotNull
        @Size(min = 2)
        String firstname,

        @NotNull
        @Size(min = 2)
        String lastname,

        @Pattern(regexp = "\\d{9,}")
        String vat,

        @NotNull
        Long sportId,

        @Valid
        @NotNull
        UserUpdateDTO userUpdateDTO,

        @Valid
        @NotNull
        PersonalInfoUpdateDTO personalInfoUpdateDTO
) {
}
