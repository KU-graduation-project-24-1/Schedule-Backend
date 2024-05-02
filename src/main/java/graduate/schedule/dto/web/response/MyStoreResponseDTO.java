package graduate.schedule.dto.web.response;

import graduate.schedule.dto.store.MyStoreDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MyStoreResponseDTO {
    private List<MyStoreDTO> myStores;
}
