package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class ChangeScheduleRequestDTO {
    private Long scheduleId;
    private Long employeeId;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
