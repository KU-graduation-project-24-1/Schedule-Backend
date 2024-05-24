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
    @Column(name = "work_time_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private Time startTime;

    private Time endTime;

    public StoreAvailableTimeByDay(Member member, Store store, DayOfWeek dayOfWeek, Time startTime, Time endTime) {
        this.member = member;
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateWorkTime(Time startTime, Time endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
