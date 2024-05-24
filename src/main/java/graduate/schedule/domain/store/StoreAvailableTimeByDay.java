package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.DayOfWeek;

@Entity
@Getter
@NoArgsConstructor
public class StoreAvailableTimeByDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "available_time_by_day_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Temporal(TemporalType.TIME)
    private Time startTime;

    @Temporal(TemporalType.TIME)
    private Time endTime;

    public static StoreAvailableTimeByDay createStoreAvailableTimeByDay(Store store, Member member, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
        StoreAvailableTimeByDay availableTimeByDay = new StoreAvailableTimeByDay();
        availableTimeByDay.member = member;
        availableTimeByDay.store = store;
        availableTimeByDay.dayOfWeek = dayOfWeek;
        availableTimeByDay.startTime = startTime;
        availableTimeByDay.endTime = endTime;

        store.addMemberAvailableTimeByDay(availableTimeByDay);

        return availableTimeByDay;
    }

    public StoreAvailableTimeByDay(Store store, Member member, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
        this.store = store;
        this.member = member;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateWorkTime(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
