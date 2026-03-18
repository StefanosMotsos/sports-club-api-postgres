package club.sportsapp.api;

import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.core.exceptions.EntityInvalidArgumentException;
import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.core.exceptions.ValidationException;
import club.sportsapp.dto.UserInsertDTO;
import club.sportsapp.dto.UserReadOnlyDTO;
import club.sportsapp.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final IUserService userService;

    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> registerUser(@RequestBody UserInsertDTO userInsertDTO, BindingResult bindingResult)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        //user validator

        if (bindingResult.hasErrors()) throw new ValidationException("User", "User invalid data", bindingResult);

        UserReadOnlyDTO userReadOnlyDTO =  userService.saveUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }

    public ResponseEntity<UserReadOnlyDTO> getUserByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(userService.getUserByUuid(uuid));
    }
}
