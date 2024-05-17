package graduate.schedule.dto.web.response.executive;

import graduate.schedule.domain.store.StoreSchedule;
import lombok.Getter;

import java.sql.Date;

import static graduate.schedule.utils.DateAndTimeFormatter.timeWithoutSeconds;

@Getter
public class ChangeScheduleResponseDTO {
    private boolean isDeleted;
    private Long scheduleId;
    private Long employeeId;
    private Date date;
    private String startTime;
    private String endTime;

    public ChangeScheduleResponseDTO() {
        this.isDeleted = true;
    }

    public ChangeScheduleResponseDTO(StoreSchedule schedule) {
        this.isDeleted = false;
        this.scheduleId = schedule.getId();
        this.employeeId = schedule.getEmployeeId();
        this.date = schedule.getDate();
        this.startTime = timeWithoutSeconds(schedule.getStartTime());
        this.endTime = timeWithoutSeconds(schedule.getEndTime());
    }
}
