package club.sportsapp.dto;

import java.util.Map;

public record ValidationErrorResponseDTO(String code, String message, Map<String, String> errors) {
}
