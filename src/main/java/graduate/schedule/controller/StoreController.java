package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.dto.web.response.CreateStoreRequestDTO;
import graduate.schedule.dto.web.response.CreateStoreResponseDTO;
import graduate.schedule.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("store")
public class StoreController {
    private final StoreService storeService;

    /**
     * @apiNote 가게 생성 api
     * */
    @PostMapping
    public BaseResponse<CreateStoreResponseDTO> createStore(@RequestBody @Valid CreateStoreRequestDTO storeRequest) {
        CreateStoreResponseDTO response = storeService.createStore(storeRequest);
        return new BaseResponse<>(response);
    }
}
