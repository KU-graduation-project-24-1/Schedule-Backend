package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.response.member.MyStoreResponseDTO;
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
    public BaseResponse<MyStoreResponseDTO> getMyStores(@MemberId @Valid Member member) {
        MyStoreResponseDTO response = memberService.getMyStores(member);
        return new BaseResponse<>(response);
    }
}
