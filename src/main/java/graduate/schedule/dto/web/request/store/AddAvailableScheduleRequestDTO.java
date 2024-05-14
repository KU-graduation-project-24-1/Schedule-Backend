package graduate.schedule.dto.web.request.store;

import lombok.Getter;

import java.sql.Date;

@Getter
public class AddAvailableScheduleRequestDTO {
    private Long memberId;
    private Long storeId;
    private Date date; //yyyy-MM-dd
    private String startTime; //HH:mm
    private String endTime; //HH:mm

}
