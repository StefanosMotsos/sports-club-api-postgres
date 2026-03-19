package club.sportsapp.dto;

public record ErrorResponseDTO(String code, String description) {

    public ErrorResponseDTO(String code) {
        this(code, "");
    }
}
