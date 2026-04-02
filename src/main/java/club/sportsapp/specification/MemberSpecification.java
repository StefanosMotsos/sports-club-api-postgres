package club.sportsapp.specification;

import club.sportsapp.core.filters.MemberFilters;
import club.sportsapp.model.Member;
import org.springframework.data.jpa.domain.Specification;

public class MemberSpecification {

    public static Specification<Member> build(MemberFilters filters) {
        return Specification.allOf(
                hasLastname(filters.getLastname()),
                hasSport(filters.getSport()),
                isDeleted(filters.isDeleted())
        );
    }

    private static Specification<Member> hasLastname(String lastname) {
        return (root, query, cb) -> lastname == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("lastname")), lastname.toLowerCase() + "%");
    }

    private static Specification<Member> hasSport(String sport) {
        return (root, query, cb) -> sport == null ? cb.conjunction() :
                cb.like(cb.lower(root.get("sport").get("name")), sport.toLowerCase() + "%");
    }

    private static Specification<Member> isDeleted(boolean deleted) {
        return (root, query, cb) -> cb.equal(root.get("deleted"), deleted);
    }
}
