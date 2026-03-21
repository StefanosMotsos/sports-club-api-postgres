package club.sportsapp.api;

import club.sportsapp.authentication.AuthenticationService;
import club.sportsapp.dto.AuthenticationRequestDTO;
import club.sportsapp.dto.AuthenticationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final AuthenticationService authenticationService;

    public ResponseEntity<AuthenticationResponseDTO> authenticate(AuthenticationRequestDTO dto) {

        AuthenticationResponseDTO authenticationResponseDTO = authenticationService.authenticate(dto);
        return ResponseEntity.ok(authenticationResponseDTO);
    }
}
