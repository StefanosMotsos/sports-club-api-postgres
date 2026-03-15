package club.sportsapp.repository;

import club.sportsapp.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, Long>,
        JpaSpecificationExecutor<Member> {

    Optional<Member> findByUuid(UUID uuid);
    Optional<Member> findByVat(String vat);
    Optional<Member> findByPersonalInfo_MembershipId(String membershipId);

    Optional<Member> findByUuidAndDeletedFalse(UUID uuid);
    Optional<Member> findByVatAndDeletedFalse(String vat);

    @EntityGraph(attributePaths = {"personalInfo", "sport"})
    Page<Member> findAllByDeletedFalse(Pageable pageable);

    boolean existsByUuidAndUserUuid(UUID memberUuid, UUID userUuid);
}
