package club.sportsapp.dto;

public record MemberReadOnlyDTO(String uuid, String firstname,
                                String lastname, String vat, String sport, String identityNumber,
                                String membershipId, String username) {
}
