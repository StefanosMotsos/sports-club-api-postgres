package club.sportsapp.api;

import club.sportsapp.core.exceptions.*;
import club.sportsapp.dto.MemberInsertDTO;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.dto.MemberUpdateDTO;
import club.sportsapp.service.IMemberService;
import club.sportsapp.validator.MemberInsertValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberRestController {

    private final IMemberService memberService;
    private final MemberInsertValidator memberInsertValidator;

    @PostMapping
    public ResponseEntity<MemberReadOnlyDTO> saveMember(@Valid @RequestBody MemberInsertDTO dto,
                                                        BindingResult bindingResult)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException {

        memberInsertValidator.validate(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Member", "Invalid member data", bindingResult);
        }

        MemberReadOnlyDTO memberReadOnlyDTO = memberService.saveMember(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("{uuid}")
                .buildAndExpand(memberReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(memberReadOnlyDTO);
    }

    @PostMapping("/{uuid}/membership-file")
    public ResponseEntity<Void> uploadMembershipFile(@PathVariable UUID uuid, @RequestParam("file") MultipartFile file)
            throws FileUploadException, EntityNotFoundException {

        memberService.saveMembershipIdFile(uuid, file);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> updateMember(@Valid @RequestBody MemberUpdateDTO dto, BindingResult bindingResult)
        throws ValidationException, EntityAlreadyExistsException, EntityNotFoundException, EntityInvalidArgumentException {



        if (bindingResult.hasErrors()) {
            throw new ValidationException("Member", "Invalid member data", bindingResult);
        }

        MemberReadOnlyDTO memberReadOnlyDTO = memberService.updateMember(dto);

        return ResponseEntity.ok(memberReadOnlyDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> deleteMemberByUuid(@PathVariable UUID uuid) throws EntityNotFoundException {

        MemberReadOnlyDTO memberReadOnlyDTO = memberService.deleteMemberByUUID(uuid);

        return ResponseEntity.ok(memberReadOnlyDTO);
    }

    //@GetMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> getMemberByUuid(@PathVariable UUID uuid) throws EntityNotFoundException {

        MemberReadOnlyDTO memberReadOnlyDTO = memberService.getMemberByUUID(uuid);

        return ResponseEntity.ok(memberReadOnlyDTO);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<MemberReadOnlyDTO> getMemberByUuidAndDeletedFalse(@PathVariable UUID uuid) throws EntityNotFoundException {

        MemberReadOnlyDTO memberReadOnlyDTO = memberService.getMemberByUUIDAndDeletedFalse(uuid);

        return ResponseEntity.ok(memberReadOnlyDTO);
    }

}
