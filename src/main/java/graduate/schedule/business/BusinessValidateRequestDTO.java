package graduate.schedule.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BusinessValidateRequestDTO {
    private List<BusinessDataDTO> businesses;
}
