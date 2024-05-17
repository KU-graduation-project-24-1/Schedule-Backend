package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;

import java.sql.Date;
import java.sql.Time;

import static graduate.schedule.utils.DateAndTimeFormatter.timeWithSeconds;

@Slf4j
@Entity
@Getter
@DynamicInsert
public class StoreSchedule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_schedule_id")
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

    @Column(columnDefinition = "boolean default false")
    private boolean requestCover;

    public static StoreSchedule createStoreSchedule(Store store, Member employee, Date date, String startTime, String endTime) {
        StoreSchedule storeSchedule = new StoreSchedule();

        storeSchedule.store = store;
        storeSchedule.employee = employee;
        storeSchedule.date = date;
        storeSchedule.startTime = timeWithSeconds(startTime);
        storeSchedule.endTime = timeWithSeconds(endTime);

        return storeSchedule;
    }

    public void setRequestCover(boolean requestCover) {
        this.requestCover = requestCover;
    }

    public void setEmployee(Member member) {
        if (!this.employee.equals(member)) {
            log.info("근무자를 수정합니다.");
            this.employee = member;
        }
    }

    public void setWorkingTime(Time startTime, Time endTime) {
        if (!this.startTime.equals(startTime)) {
            log.info("근무자 시작 시간을 수정합니다.");
            this.startTime = startTime;
        }
        if (!this.endTime.equals(endTime)) {
            log.info("근무자 마감 시간을 수정합니다.");
            this.endTime = endTime;
        }
    }

    public Long getEmployeeId() {
        return this.employee.getId();
    }

    public String getEmployessName() {
        return this.employee.getName();
    }

}
