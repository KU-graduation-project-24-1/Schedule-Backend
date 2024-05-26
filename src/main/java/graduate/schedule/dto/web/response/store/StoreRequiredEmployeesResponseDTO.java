package graduate.schedule.dto.web.response.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@AllArgsConstructor
public class StoreRequiredEmployeesResponseDTO {
    private Long storeId;
    private DayOfWeek dayOfWeek;
    private int requiredEmployees;
}
