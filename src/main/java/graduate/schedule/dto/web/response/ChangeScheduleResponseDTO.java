package graduate.schedule.dto.web.response;

import graduate.schedule.domain.store.StoreSchedule;
import lombok.Getter;

import java.sql.Date;

import static graduate.schedule.utils.DateAndTimeFormatter.timeDeleteSeconds;

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
        this.employeeId = schedule.getMemberId();
        this.date = schedule.getDate();
        this.startTime = timeDeleteSeconds(schedule.getStartTime());
        this.endTime = timeDeleteSeconds(schedule.getEndTime());
    }
}
