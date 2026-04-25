package club.sportsapp.repository;

import club.sportsapp.model.Member;
import club.sportsapp.model.MemberActivity;
import club.sportsapp.model.Role;
import club.sportsapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Member existingMember;
    private User existingUser;

    @BeforeEach
    void setup() { createDummyData();}

    @Test
    void persistAndGetMember() {
        Role role = createRole("MEMBER");

        User user = new User();
        user.setUsername("lamp_" + UUID.randomUUID());
        user.setPassword("Cod1ngF@");
        role.addUser(user);

        Member member = new Member();
        member.setFirstname("Λαμπρινή");
        member.setLastname("Παπαδοπούλου");
        member.setVat("999999999");
        member.setActivity(MemberActivity.ACTIVE);
        member.addUser(user);
        memberRepository.save(member);

        Optional<Member> found = memberRepository.findByVat("999999999");
        assertTrue(found.isPresent());
        assertEquals("Παπαδοπούλου", found.get().getLastname());
    }

    @Test
    void updateMember() {
        Member memberToUpdate =  memberRepository.findById(existingMember.getId())
                .orElseThrow();

        memberToUpdate.setFirstname("Μαρία");
        memberToUpdate.setVat("987654321");
        memberRepository.save(memberToUpdate);

        Member updated = memberRepository.findById(existingMember.getId()).orElseThrow();
        assertEquals("Μαρία", updated.getFirstname());
        assertEquals("987654321", updated.getVat());
    }

    @Test
    void getMemberByIdPositive() {
        Member member = memberRepository.findById(existingMember.getId()).orElse(null);
        assertNotNull(member);
        assertEquals("Γιαννούτσου", member.getLastname());
    }

    @Test
    void getMemberByIdNegative() {
        Member member = memberRepository.findById(9999L).orElse(null);
        assertNull(member);
    }

    @Test
    void findByVat() {
        Optional<Member> found = memberRepository.findByVat("107856341");
        assertTrue(found.isPresent());
        assertEquals(existingMember.getId(), found.get().getId());
    }

    @Test
    void findByUuid() {
        Optional<Member> found = memberRepository.findByUuid(existingMember.getUuid());
        assertTrue(found.isPresent());
        assertEquals(existingUser.getId(), found.get().getUser().getId());
    }


    private void createDummyData() {
        Role role = createRole("MEMBER");

        existingUser = new User();
        existingUser.setUsername("anna_" + UUID.randomUUID());
        existingUser.setPassword("secret");
        role.addUser(existingUser);

        existingMember = new Member();
        existingMember.setFirstname("Άννα");
        existingMember.setLastname("Γιαννούτσου");
        existingMember.setVat("107856341");
        existingMember.setActivity(MemberActivity.ACTIVE);
        existingMember.addUser(existingUser);

        memberRepository.save(existingMember);
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(name + "_" + UUID.randomUUID());
        roleRepository.save(role);
        return role;
    }
}