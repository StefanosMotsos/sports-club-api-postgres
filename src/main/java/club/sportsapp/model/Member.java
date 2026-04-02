package club.sportsapp.model;

import club.sportsapp.model.static_data.MembershipType;
import club.sportsapp.model.static_data.Sport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "members")
public class Member extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true, columnDefinition = "UUID")
    private UUID uuid;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String vat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id")
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberActivity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_type_id")
    private MembershipType membershipType;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "personal_info_id", unique = true)
    private PersonalInfo personalInfo;

    @PrePersist
    public void initializeUUIDAndActivity() {
        this.uuid = UUID.randomUUID();
        this.activity = MemberActivity.ACTIVE;
    }

    public void addUser(User user) {
        this.user = user;
        user.setMember(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) return false;
        return Objects.equals(getUuid(), member.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }
}
