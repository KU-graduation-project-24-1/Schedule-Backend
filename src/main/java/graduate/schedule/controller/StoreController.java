package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    // TODO: 5/3/24 사업자 증빙 api
    // 1. 이미 존재하는 가게인지 검사
    // 2. 사업자 진위 여부 검사 - 오픈 api
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
        return new BaseResponse<>("가게 참가에 성공하였습니다.");
    }

}
