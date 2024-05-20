package graduate.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import graduate.schedule.common.exception.FCMException;
import graduate.schedule.dto.fcm.FCMMessage;
import graduate.schedule.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class FCMService {
    // TODO: 5/12/24 FCM 푸시 알림 테스트 필요
    private final MemberRepository memberRepository;

    @Value("${firebase.api.url}")
    private String FIREBASE_API_URL;

    @Value("${firebase.key.path}")
    private String FCM_PRIVATE_KEY_PATH;

    private final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private final ObjectMapper objectMapper;

    public void sendMessageTo(String fcmToken, String title, String body) {
        try {
            String message = makeMessage(fcmToken, title, body);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message,
                    MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(FIREBASE_API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            Response response = client.newCall(request).execute();
            log.info("fcm response body: {}", response.body().string());
        } catch (IOException e) {
            throw new FCMException(FCM_SEND_ERROR);
        }
    }

    private String makeMessage(String targetToken, String title, String body) {
        try {
            FCMMessage fcmMessage = FCMMessage.builder()
                    .message(FCMMessage.Message.builder()
                            .token(targetToken)
                            .notification(FCMMessage.Notification.builder()
                                    .title(title)
                                    .body(body)
                                    .build()
                            ).build()).validateOnly(false).build();

            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new FCMException(FCM_MAKE_MESSAGE_ERROR);
        }
    }

    private String getAccessToken() {
        try {
            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                    .createScoped(List.of(FIREBASE_SCOPE));

            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new FCMException(FCM_GET_ACCESS_TOKEN_ERROR);
        }
    }
}


/*
2)ObjectMapper: FcmMessage -> Json
3)sendMessage : 토큰, title, body -> request to Firebase
4)makeMessage: FcmMessage 이용 -> to Json format / title, body 직접 입력 가능, 실제로 메시지 전송 -> validate false 고정
5)getAccessToken (): 파이어베이스에 정당히 메시지를 보낼 권리가 있다는 것을 알릴 필요가 있다. 디바이스 토큰이 아님
* */