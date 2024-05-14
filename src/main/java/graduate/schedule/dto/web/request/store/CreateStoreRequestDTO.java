package graduate.schedule.dto.web.request.store;

import lombok.Getter;

@Getter
public class CreateStoreRequestDTO {
    private Long memberId;
    private String storeName;
    private String businessRegistrationNumber;
}
