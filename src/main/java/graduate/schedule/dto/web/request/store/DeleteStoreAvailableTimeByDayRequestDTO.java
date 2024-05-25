package graduate.schedule.dto.web.request.store;

import lombok.Getter;


@Getter
public class DeleteStoreAvailableTimeByDayRequestDTO {
    private Long storeId;
    private Long storeAvailableTimeByDayId;
}
