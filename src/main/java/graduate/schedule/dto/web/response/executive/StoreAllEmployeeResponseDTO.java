package graduate.schedule.dto.web.response.executive;

import graduate.schedule.dto.store.EmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StoreAllEmployeeResponseDTO {
    private List<EmployeeDTO> employees;
}
