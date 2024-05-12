package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Date;
import java.sql.Time;

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

    @Temporal(TemporalType.TIME)
    private Time availableStartTime;
    @Temporal(TemporalType.TIME)
    private Time availableEndTime;

    public static StoreMemberAvailableTime createStoreMemberAvailableTime(Store store, Member member, Date availableDate, Time availableStartTime, Time availableEndTime) {
        StoreMemberAvailableTime availableTime = new StoreMemberAvailableTime();
        availableTime.store = store;
        availableTime.member = member;
        availableTime.availableDate = availableDate;
        availableTime.availableStartTime = availableStartTime;
        availableTime.availableEndTime = availableEndTime;

        store.addMemberAvailableTime(availableTime);

        return availableTime;
    }
}
