package graduate.schedule.controller;

import graduate.schedule.common.response.BaseResponse;
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
@RequestMapping("/api/v1/fcm")
public class FCMController {
    private final FirebaseCloudMessageService fcmService;

    @PostMapping("/send")
    public BaseResponse<String> pushMessage(@RequestBody @Valid FcmSendRequestDTO fcmSendRequestDTO) throws IOException {
        String response = fcmService.sendMessageTo(fcmSendRequestDTO);

        return new BaseResponse<>(response);
    }
}
