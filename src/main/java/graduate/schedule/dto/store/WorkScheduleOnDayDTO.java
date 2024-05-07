package graduate.schedule.dto.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;
import java.util.List;

@Getter
@AllArgsConstructor
public class WorkScheduleOnDayDTO {
    private Date date;
//    private WorkingHeadCountDTO workingHeadCount;
    private List<WorkerAndTimeDTO> workDatas;
}
