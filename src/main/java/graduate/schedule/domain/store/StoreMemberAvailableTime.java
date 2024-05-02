package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Date;

@Entity
@Getter
public class StoreMemberAvailableTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_available_time_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Temporal(TemporalType.DATE)
    private Date availableDate;

    @Column(length = 500)
    private String availableTime;
}
