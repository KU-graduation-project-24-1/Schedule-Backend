package graduate.schedule.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import graduate.schedule.common.exception.MemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.dto.fcm.FCMMessage;
import graduate.schedule.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_FOUND_MEMBER;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    // TODO: 5/12/24 FCM 푸시 알림 테스트 필요
    private final MemberRepository memberRepository;

    @Value("${firebase.project.id}")
    private String FCM_PROJECT_ID;

    @Value("${firebase.key.path}")
    private String FCM_PRIVATE_KEY_PATH;

    private FirebaseMessaging firebaseMessaging;
    private String FIREBASE_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private final String API_URL = "https://fcm.googleapis.com/v1/projects/" + FCM_PROJECT_ID + "/messages:send";
    private final ObjectMapper objectMapper;


    //fcm 기본 설정
    @PostConstruct
    public void fcmInit() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                .createScoped((Arrays.asList(FIREBASE_SCOPE)));
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase 어플리케이션을 초기화 했습니다.");
        }
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions);
        this.firebaseMessaging = FirebaseMessaging.getInstance(app);
    }

    public void sendMessageTo(Long memberId, String title, String body) throws IOException {
        //title을 어떤 알림인지 enum으로?
        String fcmToken = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER))
                .getFcmToken();

        String message = makeMessage(fcmToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }

    private String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        FCMMessage fcmMessage = FCMMessage.builder()
                .message(FCMMessage.Message.builder()
                        .token(targetToken)
                        .notification(FCMMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                .createScoped(List.of(FIREBASE_SCOPE));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}


/*
2)ObjectMapper: FcmMessage -> Json
3)sendMessage : 토큰, title, body -> request to Firebase
4)makeMessage: FcmMessage 이용 -> to Json format / title, body 직접 입력 가능, 실제로 메시지 전송 -> validate false 고정
5)getAccessToken (): 파이어베이스에 정당히 메시지를 보낼 권리가 있다는 것을 알릴 필요가 있다. 디바이스 토큰이 아님
* */