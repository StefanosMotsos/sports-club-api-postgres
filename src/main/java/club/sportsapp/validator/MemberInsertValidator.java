package club.sportsapp.validator;

import club.sportsapp.dto.MemberInsertDTO;
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
public class MemberInsertValidator implements Validator {

    private final MemberServiceImpl memberService;
    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberInsertDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberInsertDTO memberInsertDTO = (MemberInsertDTO) target;

        if (memberService.isMemberExists(memberInsertDTO.vat())) {
            log.warn("Save failed, member with vat={} already exists", memberInsertDTO.vat());
            errors.rejectValue("vat", "member.already.exists", "Member with vat " + memberInsertDTO.vat() + " already exists");
        }

        if (personalInfoRepository.findByMembershipId(memberInsertDTO.personalInfoInsertDTO().membershipId()).isPresent()) {
            log.warn("Save failed, member with membership={} already exists", memberInsertDTO.personalInfoInsertDTO().membershipId());
            errors.rejectValue("membershipId", "member.already.exists", "Member with membership " + memberInsertDTO.personalInfoInsertDTO().membershipId() + " already exists");
        }

        if (personalInfoRepository.findByIdentityNumber(memberInsertDTO.personalInfoInsertDTO().identityNumber()).isPresent()) {
            log.warn("Save failed, member with identity number={} already exists", memberInsertDTO.personalInfoInsertDTO().identityNumber());
            errors.rejectValue("identityNumber", "member.already.exists", "Member with identity number " + memberInsertDTO.personalInfoInsertDTO().identityNumber() + " already exists");
        }

        if (userRepository.findByUsername(memberInsertDTO.userInsertDTO().username()).isPresent()) {
            log.warn("Save failed, member with username={} already exists", memberInsertDTO.userInsertDTO().username());
            errors.rejectValue("username", "member.already.exists", "Member with username " + memberInsertDTO.userInsertDTO().username() + " already exists");
        }
    }
}
