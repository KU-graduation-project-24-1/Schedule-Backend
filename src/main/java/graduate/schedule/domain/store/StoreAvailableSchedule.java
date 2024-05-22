package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Date;
import java.sql.Time;

@Entity
@Getter
public class StoreAvailableSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "available_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member employee;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.TIME)
    private Time startTime;
    @Temporal(TemporalType.TIME)
    private Time endTime;

    public static StoreAvailableSchedule createStoreAvailableSchedule(Store store, Member employee, Date availableDate, Time availableStartTime, Time availableEndTime) {
        StoreAvailableSchedule availableSchedule = new StoreAvailableSchedule();
        availableSchedule.store = store;
        availableSchedule.employee = employee;
        availableSchedule.date = availableDate;
        availableSchedule.startTime = availableStartTime;
        availableSchedule.endTime = availableEndTime;

        store.addMemberAvailableTime(availableSchedule);

        return availableSchedule;
    }
}
