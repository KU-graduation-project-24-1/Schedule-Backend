package graduate.schedule.dto.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class AvailableScheduleInDayDTO {
    private Date date;
    private List<AvailableTimeInDayDTO> availableTimesInDay;
}
