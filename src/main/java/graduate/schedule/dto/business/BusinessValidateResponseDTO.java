package graduate.schedule.dto.business;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessValidateResponseDTO {
    private int request_cnt; //조회 요청 수
    private int valid_cnt; //검증 Valid 수
    private String status_code;
    private List<BusinessValidatedDTO> data;
}
