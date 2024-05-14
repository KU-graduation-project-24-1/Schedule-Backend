package graduate.schedule.controller;

import graduate.schedule.annotation.MemberId;
import graduate.schedule.common.response.BaseResponse;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.web.request.FcmSendRequestDTO;
import graduate.schedule.service.FirebaseCloudMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class FCMController {
    private final FirebaseCloudMessageService fcmService;

    @PostMapping
    public BaseResponse<String> pushMessage(@MemberId @Valid Member member, @RequestBody @Valid FcmSendRequestDTO fcmSendRequestDTO) throws IOException {
        fcmService.sendMessageTo(member, fcmSendRequestDTO);

        return new BaseResponse<>("푸시 알림 전송에 성공하였습니다.");
    }
}
