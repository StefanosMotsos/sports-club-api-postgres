package club.sportsapp.security;

import club.sportsapp.model.User;
import club.sportsapp.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
public class SecurityService {

    @Autowired
    private MemberRepository memberRepository;

    public boolean isOwnMemberProfile(UUID memberUuid, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return memberRepository.existsByUuidAndUser_Uuid(memberUuid, principal.getUuid());
    }
}
