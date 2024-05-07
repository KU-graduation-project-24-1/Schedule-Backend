package graduate.schedule.domain.store;

import graduate.schedule.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.DynamicInsert;

import java.sql.Date;
import java.sql.Time;

@Entity
@Getter
@DynamicInsert
public class StoreMemberWorkingTime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_working_time_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.TIME)
    private Time startTime;
    @Temporal(TemporalType.TIME)
    private Time endTime;

    @Column(columnDefinition = "boolean default false")
    private boolean requestCover;
}
