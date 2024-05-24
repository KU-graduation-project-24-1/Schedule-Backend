package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.store.*;
import graduate.schedule.dto.web.response.store.*;
import graduate.schedule.dto.web.request.store.CreateStoreRequestDTO;
import graduate.schedule.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public BaseResponse<CreateStoreResponseDTO> createStore(@MemberId @Valid Member member, @RequestBody @Valid CreateStoreRequestDTO storeRequest) {
        CreateStoreResponseDTO response = storeService.createStore(member, storeRequest);
        return new BaseResponse<>(response);
    }
    /**
     * @apiNote 초대 코드 재발급 api
     * */
    @PostMapping("/invite-code")
    public BaseResponse<RegenerateInviteCodeResponseDTO> createStore(@MemberId @Valid Member member, @RequestBody @Valid RequestWithOnlyStoreIdDTO storeRequest) {
        RegenerateInviteCodeResponseDTO response = storeService.regenerateInviteCode(member, storeRequest);
        return new BaseResponse<>(response);
    }


    /**
     * @apiNote 가게 참가 전 초대 코드 유효 여부 및 가게 조회 api
     * */
    @GetMapping("/invite-code")
    public BaseResponse<SearchStoreWithInviteCodeResponseDTO> searchStoreWithInviteCode(@RequestBody @Valid SearchStoreWithInviteCodeRequestDTO storeRequest) {
        SearchStoreWithInviteCodeResponseDTO response = storeService.searchStoreWithInviteCode(storeRequest.getInviteCode());
        return new BaseResponse<>(response);
    }
    /**
     * @apiNote 가게 참가 api
     * */
    @PostMapping("/join")
    public BaseResponse<String> joinTeam(@MemberId @Valid Member member, @RequestBody @Valid RequestWithOnlyStoreIdDTO storeRequest) {
        storeService.joinStore(member, storeRequest);
        return new BaseResponse<>(ENTER_TO_STORE.getMessage());
    }

    /**
     * @apiNote 특정 달의 모든 근무 일정 조회 api
     * */
    @GetMapping("/{storeId}/schedule/{searchMonth}") //yyyy-MM
    public BaseResponse<WorkScheduleOnMonthResponseDTO> getScheduleOnMonth(@MemberId @Valid Member member, @PathVariable @Valid Long storeId, @PathVariable @Valid String searchMonth) {
        WorkScheduleOnMonthResponseDTO response = storeService.getScheduleOnMonth(member, storeId, searchMonth);
        return new BaseResponse<>(response);
    }

    /**
     * @apiNote 특정 멤버의 특정 달에 근무 가능한 시간 조회 api
     * */
    @GetMapping("/{storeId}/available-schedule/{searchMonth}")
    public BaseResponse<AvailableScheduleOnMonthResponseDTO> getAvailableScheduleOnMonth(@MemberId @Valid Member member, @PathVariable @Valid Long storeId, @PathVariable @Valid String searchMonth) {
        AvailableScheduleOnMonthResponseDTO response = storeService.getAvailableScheduleOnMonth(member, storeId, searchMonth);
        return new BaseResponse<>(response);
    }

    /**
    * @apiNote 일 단위 근무 가능한 시간 추가 api
    */
    @PostMapping("/available-schedule")
    public BaseResponse<AddAvailableScheduleResponseDTO> addAvailableScheduleInDay(@MemberId @Valid Member member, @RequestBody @Valid AddAvailableScheduleRequestDTO storeRequest) {
        AddAvailableScheduleResponseDTO response = storeService.addAvailableScheduleInDay(member, storeRequest);
        return new BaseResponse<>(response);
    }

    /**
    * @apiNote 일 단위 근무 가능한 시간 삭제 api
    */
    @DeleteMapping("/available-schedule")
    public BaseResponse<String> deleteAvailableScheduleInDay(@MemberId @Valid Member member, @RequestBody @Valid DeleteAvailableScheduleRequestDTO storeRequest) {
        storeService.deleteAvailableScheduleInDay(member, storeRequest);
        return new BaseResponse<>(SUCCESS_DELETE_AVAILABLE_SCHEDULE.getMessage());
    }

    // 특정 가게의 내 정보 가져오기
    @GetMapping("/{storeId}/my-info")
    public BaseResponse<MyStoreInfoResponseDTO> getMyStoreInfo(@MemberId @Valid Member member, @PathVariable @Valid Long storeId) {
        MyStoreInfoResponseDTO response = storeService.getMyStoreInfo(member, storeId);
        return new BaseResponse<>(response);
    }

    // 주 단위 고정 근무시간 가져오기
    @GetMapping("/fixed-schedule")
    public BaseResponse<StoreAvailableTimeByDayResponseDTO> getStoreAvailableTimeByDay(@PathVariable Long storeId, @MemberId Member member) {
        StoreAvailableTimeByDayResponseDTO response = storeService.getStoreAvailableTimeByDay(member, storeId);
        return new BaseResponse<>(response);
    }

}
