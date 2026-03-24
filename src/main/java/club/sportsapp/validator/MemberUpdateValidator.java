package club.sportsapp.validator;

import club.sportsapp.core.exceptions.EntityNotFoundException;
import club.sportsapp.dto.MemberReadOnlyDTO;
import club.sportsapp.dto.MemberUpdateDTO;
import club.sportsapp.model.Member;
import club.sportsapp.repository.PersonalInfoRepository;
import club.sportsapp.repository.UserRepository;
import club.sportsapp.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberUpdateValidator implements Validator {

    private final MemberServiceImpl memberService;
    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberUpdateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberUpdateDTO dto = (MemberUpdateDTO) target;

        try {
            MemberReadOnlyDTO memberReadOnlyDTO = memberService.getMemberByUUIDAndDeletedFalse(dto.uuid());

            if (memberReadOnlyDTO != null && !memberReadOnlyDTO.vat().equals(dto.vat())) {
                if (memberService.isMemberExists(dto.vat())) {
                    log.warn("Update failed, member with vat={} already exists", dto.vat());
                    errors.rejectValue("vat", "member.already.exists", "Member with vat " + dto.vat() + " already exists");
                }
            }

            if (memberReadOnlyDTO != null && !memberReadOnlyDTO.membershipId().equals(dto.personalInfoUpdateDTO().membershipId())) {
                if (personalInfoRepository.findByMembershipId(dto.personalInfoUpdateDTO().membershipId()).isPresent()) {
                    log.warn("Update failed, member with membership={} already exists", dto.personalInfoUpdateDTO().membershipId());
                    errors.rejectValue("membershipId", "member.already.exists", "Member with membership " + dto.personalInfoUpdateDTO().membershipId() + " already exists");
                }
            }

            if (memberReadOnlyDTO != null && !memberReadOnlyDTO.identityNumber().equals(dto.personalInfoUpdateDTO().identityNumber())) {
                if (personalInfoRepository.findByMembershipId(dto.personalInfoUpdateDTO().identityNumber()).isPresent()) {
                    log.warn("Update failed, member with identity number={} already exists", dto.personalInfoUpdateDTO().identityNumber());
                    errors.rejectValue("membershipId", "member.already.exists", "Member with identity number " + dto.personalInfoUpdateDTO().identityNumber() + " already exists");
                }
            }

            if (userRepository.findByUsername(dto.userUpdateDTO().username()).isPresent()) {
                log.warn("Update failed, member with username={} already exists", dto.userUpdateDTO().username());
                errors.rejectValue("username", "member.already.exists", "Member with username " + dto.userUpdateDTO().username() + " already exists");
            }

        } catch (EntityNotFoundException e) {
            log.warn("Update failed. Member with uuid={} was not found", dto.uuid());
            errors.rejectValue("uuid", "uuid.member.notfound");
        }
    }
}
