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
    private Member employee;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

    public StoreAvailableTimeByDay(Store store, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static StoreAvailableTimeByDay createStoreAvailableTimeByDay(Store store, Member employee, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
        StoreAvailableTimeByDay availableTimeByDay = new StoreAvailableTimeByDay();
        availableTimeByDay.employee = employee;
        availableTimeByDay.store = store;
        availableTimeByDay.dayOfWeek = dayOfWeek;
        availableTimeByDay.startTime = startTime;
        availableTimeByDay.endTime = endTime;

        store.addMemberAvailableTimeByDay(availableTimeByDay);

        return availableTimeByDay;
    }

}
