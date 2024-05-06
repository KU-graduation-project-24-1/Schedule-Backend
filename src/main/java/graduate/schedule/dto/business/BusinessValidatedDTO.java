package graduate.schedule.dto.business;

import graduate.schedule.business.BusinessDataDTO;
import lombok.Getter;

@Getter
public class BusinessValidatedDTO {
    private String b_no;
    private String valid;
    private String valid_msg;
    private BusinessDataDTO request_param;
}
