package graduate.schedule.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_name")
    private String name;

    @Column(name = "member_email")
    private String email;

    @Column(name = "member_image")
    private String profileImg;

    @Column(length = 500)
    private String platformId;

    private String fcmToken;

    public static Member createMember(String email, String profileImg, String platformId, String fcmToken) {
        Member member = new Member();

        member.email = email;
        member.profileImg = profileImg;
        member.platformId = platformId;
        member.fcmToken = fcmToken;

        return member;
    }
}
