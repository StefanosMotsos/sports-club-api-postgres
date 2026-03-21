package club.sportsapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import club.sportsapp.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Authentication Filter. Returns 401.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {

        log.warn("User not authenticated, with message={}", e.getMessage());

        String errorCode = switch (e.getClass().getSimpleName()) {
            case "BadCredentialsException" -> "BAD_CREDENTIALS";
            case "DisabledException" -> "ACCOUNT_DISABLED";
            case "LockedException" -> "ACCOUNT_LOCKED";
            case "AccountExpiredException" -> "ACCOUNT_EXPIRED";
            case "CredentialsExpiredException" -> "CREDENTIALS_EXPIRED";
            default -> "UNAUTHORIZED";
        };

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new ErrorResponseDTO(errorCode, e.getMessage())
                )
        );
    }
}
