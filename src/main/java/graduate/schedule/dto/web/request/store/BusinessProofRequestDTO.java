package graduate.schedule.dto.web.request.store;

import lombok.Getter;

@Getter
public class BusinessProofRequestDTO {
    private String businessRegistrationNumber; //암호화 된 사업자등록번호
    private String ceoName;
    private String openingDate; //yyyyMMdd

}
