package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.dto.web.request.BusinessProofRequestDTO;
import graduate.schedule.dto.web.request.RegenerateInviteCodeRequestDTO;
import graduate.schedule.dto.web.request.SearchStoreWithInviteCodeRequestDTO;
import graduate.schedule.dto.web.request.JoinStoreRequestDTO;
import graduate.schedule.dto.web.response.CreateStoreRequestDTO;
import graduate.schedule.dto.web.response.CreateStoreResponseDTO;
import graduate.schedule.dto.web.response.RegenerateInviteCodeResponseDTO;
import graduate.schedule.dto.web.response.SearchStoreWithInviteCodeResponseDTO;
import graduate.schedule.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    /**
     * @apiNote 가게 존재 여부 검사 및 사업자 증빙 api
     * */
    @GetMapping("/business-proof")
    public BaseResponse<String> businessProof(@RequestBody @Valid BusinessProofRequestDTO storeRequest) {
        storeService.businessProof(storeRequest);
        return new BaseResponse<>(BUSINESS_CHECKED.getMessage());
    }

    /**
     * @apiNote 가게 생성 api
     * */
    @PostMapping
    public BaseResponse<CreateStoreResponseDTO> createStore(@RequestBody @Valid CreateStoreRequestDTO storeRequest) {
        CreateStoreResponseDTO response = storeService.createStore(storeRequest);
        return new BaseResponse<>(response);
    }
    /**
     * @apiNote 초대 코드 재발급 api
     * */
    @PostMapping("/invite-code")
    public BaseResponse<RegenerateInviteCodeResponseDTO> createStore(@RequestBody @Valid RegenerateInviteCodeRequestDTO storeRequest) {
        RegenerateInviteCodeResponseDTO response = storeService.regenerateInviteCode(storeRequest);
        return new BaseResponse<>(response);
    }


    /**
     * @apiNote 가게 참가 전 초대 코드 유효 여부 및 가게 조회 api
     * */
    @GetMapping("/inviteCode")
    public BaseResponse<SearchStoreWithInviteCodeResponseDTO> searchStoreWithInviteCode(@RequestBody @Valid SearchStoreWithInviteCodeRequestDTO storeRequest) {
        SearchStoreWithInviteCodeResponseDTO response = storeService.searchStoreWithInviteCode(storeRequest.getInviteCode());
        return new BaseResponse<>(response);
    }
    /**
     * @apiNote 가게 참가 api
     * */
    @PostMapping("/join")
    public BaseResponse<String> joinTeam(@RequestBody @Valid JoinStoreRequestDTO storeRequest) {
        storeService.joinStore(storeRequest);
        return new BaseResponse<>(ENTER_TO_STORE.getMessage());
    }

}
