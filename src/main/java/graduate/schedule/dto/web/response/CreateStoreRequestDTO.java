package graduate.schedule.dto.web.response;

import lombok.Getter;

@Getter
public class CreateStoreRequestDTO {
    private Long memberId;
    private String storeName;
    private String businessRegistrationNumber;
}
