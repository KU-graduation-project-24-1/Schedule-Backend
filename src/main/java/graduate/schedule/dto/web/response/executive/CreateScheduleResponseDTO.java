package graduate.schedule.dto.web.response.executive;

import lombok.Getter;

import java.sql.Date;

@Getter
public class CreateScheduleResponseDTO {
    private Long scheduleId;
    private Date date;
    private Long employeeId;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
