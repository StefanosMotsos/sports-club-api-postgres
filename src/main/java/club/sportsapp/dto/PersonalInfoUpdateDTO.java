package club.sportsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record PersonalInfoUpdateDTO(

        @NotNull
        @Pattern(regexp = "\\d{11}")
        String membershipId,

        @NotBlank
        String identityNumber,

        @NotBlank
        String placeOfBirth,

        @NotBlank
        String branchOfRegistration
) {
}
