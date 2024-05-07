package graduate.schedule.domain.store;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;

@Entity
@Getter
public class RequiredTimeAndHeadCount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "required_time_and_headcount_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; //요일

    @Column(columnDefinition = "boolean default true")
    private boolean needEmployee;

    private int headCount; //인원수

    @OneToMany(mappedBy = "requiredTimeAndHeadCount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequiredTimeInDay> requiredTimesInDay;
}
