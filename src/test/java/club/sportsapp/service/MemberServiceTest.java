package club.sportsapp.service;

import club.sportsapp.api.MemberRestController;
import club.sportsapp.core.exceptions.EntityAlreadyExistsException;
import club.sportsapp.dto.*;
import club.sportsapp.model.*;
import club.sportsapp.model.static_data.MembershipType;
import club.sportsapp.model.static_data.Sport;
import club.sportsapp.repository.MemberRepository;
import club.sportsapp.repository.MembershipTypeRepository;
import club.sportsapp.repository.RoleRepository;
import club.sportsapp.repository.SportRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MemberServiceTest {

    @Autowired private IMemberService memberService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private MembershipTypeRepository membershipTypeRepository;

    private Long sportId;
    private Long membershipTypeId;
    private Member existingMember;

    @BeforeAll
    void setupStaticData() {
        Role admin = new Role();
        admin.setName("ADMIN");
        roleRepository.save(admin);

        Role employee = new Role();
        employee.setName("EMPLOYEE");
        roleRepository.save(employee);

        Role member = new Role();
        member.setName("MEMBER");
        roleRepository.save(member);

        Sport sport = new Sport();
        sport.setName("Football");
        sportRepository.save(sport);
        sportId =  sport.getId();
        MembershipType basic = new MembershipType();
        basic.setName("BASIC");
        membershipTypeRepository.save(basic);
        membershipTypeId = basic.getId();
    }

    @BeforeEach
    void setup() { createDummyData();}

    private void createDummyData() {
        Role role = roleRepository.findAll().stream()
                .filter(r -> r.getName().equals("MEMBER"))
                .findFirst().orElseThrow();

        User user = new User();
        user.setUsername("anna_" + UUID.randomUUID());
        user.setPassword("secret");
        role.addUser(user);

        existingMember = new Member();
        existingMember.setUuid(UUID.randomUUID());
        existingMember.setFirstname("Άννα");
        existingMember.setLastname("Γιαννούτσου");
        existingMember.setVat("107856341");
        existingMember.setActivity(MemberActivity.ACTIVE);

        MembershipType membershipType = membershipTypeRepository.findById(membershipTypeId).orElseThrow();
        existingMember.setMembershipType(membershipType);

        Sport sport = sportRepository.findById(sportId).orElseThrow();
        existingMember.setSport(sport);

        PersonalInfo existingPersonalInfo = new PersonalInfo();
        existingPersonalInfo.setMembershipId("1111111111");
        existingPersonalInfo.setIdentityNumber("DB123456");
        existingPersonalInfo.setPlaceOfBirth("Athens");
        existingPersonalInfo.setBranchOfRegistration("Kypseli");

        existingMember.setPersonalInfo(existingPersonalInfo);

        existingMember.setUser(user);

        memberRepository.save(existingMember);
    }

    @Test
    void saveMember_successWithFile() throws Exception {
        UserInsertDTO userInsertDTO = new UserInsertDTO(
                "user_" + UUID.randomUUID(), "Cod1ngF@", 3L);
        PersonalInfoInsertDTO personalInfoInsertDTO = PersonalInfoInsertDTO.builder()
                .membershipId("2222222222").identityNumber("DB123457")
                .placeOfBirth("Athens").branchOfRegistration("Kypseli")
                .build();

        MemberInsertDTO dto = new MemberInsertDTO(
                "Stefanos", "Motsos", "222222222", sportId, membershipTypeId,
                userInsertDTO, personalInfoInsertDTO
        );

        MemberReadOnlyDTO result = memberService.saveMember(dto);

        assertNotNull(result);
        assertEquals("Stefanos", result.firstname());

        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[100]);
        memberService.saveMembershipIdFile(UUID.fromString(result.uuid()), file);

        Member found = memberRepository.findByVat("222222222").orElseThrow();
        assertEquals("Stefanos", found.getFirstname());
        assertNotNull(found.getPersonalInfo().getMembershipFile());
    }

    @Test
    void saveMember_duplicateVat_throwsException() {
        UserInsertDTO userInsertDTO = new UserInsertDTO(
                "user_" + UUID.randomUUID(), "Cod1ngF@", 3L);
        PersonalInfoInsertDTO personalInfoInsertDTO = PersonalInfoInsertDTO.builder()
                .membershipId("44444444444").identityNumber("DB123458")
                .placeOfBirth("Athens").branchOfRegistration("Kypseli")
                .build();

        MemberInsertDTO dto = new MemberInsertDTO(
                "Stefanos", "Motsos", "107856341", sportId, membershipTypeId,
                userInsertDTO, personalInfoInsertDTO
        );

        assertThrows(EntityAlreadyExistsException.class, () -> memberService.saveMember(dto));
    }

    @Test
    @WithMockUser(authorities = "EDIT_MEMBER")
    void updateTeacher_success() throws Exception {
        Member before = memberRepository.findById(existingMember.getId()).orElseThrow();

        UserUpdateDTO userUpdateDTO =  new UserUpdateDTO(before.getUser().getUsername(), "NewPass1@!");
        PersonalInfoUpdateDTO personalInfoUpdateDTO = PersonalInfoUpdateDTO.builder()
                .membershipId(before.getPersonalInfo().getMembershipId())
                .identityNumber(before.getPersonalInfo().getIdentityNumber())
                .placeOfBirth(before.getPersonalInfo().getPlaceOfBirth())
                .branchOfRegistration(before.getPersonalInfo().getBranchOfRegistration())
                .build();

        MemberUpdateDTO dto = new MemberUpdateDTO(
                before.getUuid(), "Λαμπρινή", before.getLastname(),
                before.getVat(), sportId, membershipTypeId, userUpdateDTO, personalInfoUpdateDTO);

        MemberReadOnlyDTO result = memberService.updateMember(dto);

        assertNotNull(result);
        assertEquals(before.getUuid().toString(), result.uuid());

        Member after = memberRepository.findById(before.getId()).orElseThrow();
        assertEquals("Λαμπρινή", after.getFirstname());
        assertEquals(before.getVat(), after.getVat());

    }
}
