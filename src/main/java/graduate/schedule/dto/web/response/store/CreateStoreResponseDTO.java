package graduate.schedule.dto.web.response.store;

import graduate.schedule.domain.store.Store;
import lombok.Getter;

@Getter
public class CreateStoreResponseDTO {
    private Long storeId;
    private String inviteCode;

    public CreateStoreResponseDTO(Store store) {
        this.storeId = store.getId();
        this.inviteCode = store.getInviteCode();
    }
}
