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
    @Column(name = "operationInfoId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId")
    private Store store;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    private int requiredEmployees;

    @Temporal(TemporalType.TIME)
    private Time startTime;

    @Temporal(TemporalType.TIME)
    private Time endTime;

    public StoreOperationInfo(Store store, DayOfWeek dayOfWeek, int requiredEmployees, Time startTime, Time endTime) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.requiredEmployees = requiredEmployees;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
