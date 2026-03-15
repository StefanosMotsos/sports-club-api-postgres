package club.sportsapp.repository;

import club.sportsapp.model.PersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long>,
        JpaSpecificationExecutor<PersonalInfo> {


    Optional<PersonalInfo> findByMembershipId(String membershipId);
    Optional<PersonalInfo> findByIdentityNumber(String identityNumber);
}
