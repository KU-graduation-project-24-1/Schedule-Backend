package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.dto.web.request.RequestWithOnlyMemberIdDTO;
import graduate.schedule.dto.web.response.MyStoreResponseDTO;
import graduate.schedule.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/store")
    public BaseResponse<MyStoreResponseDTO> getMyStores(@RequestBody @Valid RequestWithOnlyMemberIdDTO memberRequest) {
        MyStoreResponseDTO response = memberService.getMyStores(memberRequest.getMemberId());
        return new BaseResponse<>(response);
    }
}
