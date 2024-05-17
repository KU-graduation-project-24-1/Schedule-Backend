package graduate.schedule.dto.web.response.executive;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Date;

@Getter
@AllArgsConstructor
public class CreateScheduleResponseDTO {
    private Long scheduleId;
    private Long employeeId;
    private Date date;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
