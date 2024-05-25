package graduate.schedule.domain.store;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.time.DayOfWeek;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StoreOperationInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_info_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "required_employees", nullable = false)
    private int requiredEmployees;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;


    public StoreOperationInfo(Store store, DayOfWeek dayOfWeek, int requiredEmployees, Time startTime, Time endTime) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.requiredEmployees = requiredEmployees;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
