package club.sportsapp.mapper;

import club.sportsapp.dto.*;
import club.sportsapp.model.Member;
import club.sportsapp.model.PersonalInfo;
import club.sportsapp.model.User;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public User mapToUserEntity(UserInsertDTO userInsertDTO) {
        return new User(userInsertDTO.username(), userInsertDTO.password());
    }

    public UserReadOnlyDTO mapToUserReadOnlyDTO(User user) {
        return new UserReadOnlyDTO(user.getUuid().toString(), user.getUsername(), user.getRole().getName());
    }

    public MemberReadOnlyDTO mapToMemberReadOnlyDTO(Member member) {
        return new MemberReadOnlyDTO(member.getUuid().toString(), member.getFirstname(), member.getLastname(),
                member.getVat(), member.getSport().getName());
    }

    public Member mapToMemberEntity(MemberInsertDTO memberInsertDTO) {
        Member member = new Member();
        member.setFirstname(memberInsertDTO.firstname());
        member.setLastname(memberInsertDTO.lastname());
        member.setVat(memberInsertDTO.vat());

        UserInsertDTO userInsertDTO = memberInsertDTO.userInsertDTO();
        User user = new User(userInsertDTO.username(), userInsertDTO.password());
        member.addUser(user);

        PersonalInfoInsertDTO personalInfoInsertDTO = memberInsertDTO.personalInfoInsertDTO();
        PersonalInfo personalInfo = new PersonalInfo();

        personalInfo.setMembershipId(personalInfoInsertDTO.membershipId());
        personalInfo.setIdentityNumber(personalInfoInsertDTO.identityNumber());
        personalInfo.setPlaceOfBirth(personalInfoInsertDTO.placeOfBirth());
        personalInfo.setBranchOfRegistration(personalInfoInsertDTO.branchOfRegistration());

        member.setPersonalInfo(personalInfo);
        return member;
    }
}
