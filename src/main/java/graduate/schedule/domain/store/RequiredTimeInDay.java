package graduate.schedule.domain.store;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Time;

@Entity
@Getter
public class RequiredTimeInDay {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "required_time_in_day_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "required_time_and_headcount_id")
    private RequiredTimeAndHeadCount requiredTimeAndHeadCount;

    @Temporal(TemporalType.TIME)
    private Time startTime;
    @Temporal(TemporalType.TIME)
    private Time endTime;
}
