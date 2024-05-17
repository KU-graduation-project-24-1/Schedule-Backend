package graduate.schedule.dto.web.request.executive;

import lombok.Getter;

import java.sql.Date;

@Getter
public class CreateScheduleRequestDTO {
    private Long storeId;
    private Long employeeId;
    private Date date;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
