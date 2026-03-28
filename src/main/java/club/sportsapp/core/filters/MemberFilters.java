package club.sportsapp.core.filters;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class MemberFilters {

    private String vat;
    private String membershipId;
    private String lastname;
    private boolean deleted;
    private String sport;
}
