package unicon.Achiva.domain.push.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import unicon.Achiva.domain.push.PushErrorCode;
import unicon.Achiva.domain.push.dto.ExpoPushResponse;
import unicon.Achiva.global.response.GeneralException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpoPushClient {

    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 단일 푸시 메시지 전송
     *
     * @param expoPushToken Expo 푸시 토큰
     * @param title 알림 제목
     * @param body 알림 본문
     * @param data 추가 데이터 (선택)
     * @return ExpoPushResponse (status, ticketId, message)
     */
    public ExpoPushResponse sendPushNotification(
        String expoPushToken,
        String title,
        String body,
        Map<String, Object> data
    ) {
        // 1. Expo API 요청 페이로드 구성
        Map<String, Object> payload = Map.of(
            "to", expoPushToken,
            "sound", "default",
            "title", title,
            "body", body,
            "data", data != null ? data : Map.of()
        );

        return sendToExpo(payload);
    }

    /**
     * 배치 푸시 메시지 전송 (최대 100개)
     *
     * @param expoPushTokens 푸시 토큰 목록
     * @param title 알림 제목
     * @param body 알림 본문
     * @param data 추가 데이터 (선택)
     * @return ExpoPushResponse 목록
     */
    public List<ExpoPushResponse> sendBatchPushNotifications(
        List<String> expoPushTokens,
        String title,
        String body,
        Map<String, Object> data
    ) {
        if (expoPushTokens.size() > 100) {
            throw new IllegalArgumentException("Batch size cannot exceed 100");
        }

        // 2. 배치 요청 페이로드 구성
        List<Map<String, Object>> payloads = expoPushTokens.stream()
            .map(token -> Map.of(
                "to", (Object) token,
                "sound", (Object) "default",
                "title", (Object) title,
                "body", (Object) body,
                "data", (Object) (data != null ? data : Map.of())
            ))
            .toList();

        return sendBatchToExpo(payloads);
    }

    /**
     * Expo API로 단일 푸시 전송
     */
    private ExpoPushResponse sendToExpo(Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.debug("[Expo] 푸시 전송 요청 - payload: {}", payload);

            ResponseEntity<Map> response = restTemplate.exchange(
                EXPO_PUSH_URL,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Object rawData = body.get("data");

                // Expo 단일 전송의 경우 data가 객체, 배치 전송의 경우 배열인 형태 모두 허용
                List<Map<String, Object>> data;
                if (rawData instanceof List) {
                    // 이미 리스트인 경우 그대로 캐스팅
                    data = (List<Map<String, Object>>) rawData;
                } else if (rawData instanceof Map) {
                    // 단일 객체인 경우 리스트로 감싸서 통일
                    data = List.of((Map<String, Object>) rawData);
                } else {
                    log.error("[Expo] 예기치 않은 data 형식 - data: {}", rawData);
                    throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
                }

                if (!data.isEmpty()) {
                    Map<String, Object> firstResult = data.get(0);

                    String status = (String) firstResult.get("status");
                    String ticketId = (String) firstResult.get("id");
                    String message = (String) firstResult.get("message");

                    log.info("[Expo] 푸시 전송 응답 - status: {}, ticketId: {}", status, ticketId);

                    return ExpoPushResponse.builder()
                        .status(status)
                        .ticketId(ticketId)
                        .message(message)
                        .build();
                } else {
                    log.error("[Expo] 응답 데이터가 없음 - body: {}", body);
                    throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
                }
            } else {
                log.error("[Expo] 비정상 응답 - status: {}", response.getStatusCode());
                throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
            }

        } catch (RestClientException e) {
            log.error("[Expo] 푸시 전송 실패 - error: {}", e.getMessage(), e);
            throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
        }
    }

    /**
     * Expo API로 배치 푸시 전송
     */
    private List<ExpoPushResponse> sendBatchToExpo(List<Map<String, Object>> payloads) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(payloads, headers);

            log.debug("[Expo] 배치 푸시 전송 요청 - count: {}", payloads.size());

            ResponseEntity<Map> response = restTemplate.exchange(
                EXPO_PUSH_URL,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

                if (data != null) {
                    List<ExpoPushResponse> results = data.stream()
                        .map(result -> ExpoPushResponse.builder()
                            .status((String) result.get("status"))
                            .ticketId((String) result.get("id"))
                            .message((String) result.get("message"))
                            .build())
                        .toList();

                    log.info("[Expo] 배치 푸시 전송 완료 - count: {}", results.size());
                    return results;
                } else {
                    log.error("[Expo] 배치 응답 데이터가 없음 - body: {}", body);
                    throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
                }
            } else {
                log.error("[Expo] 비정상 응답 - status: {}", response.getStatusCode());
                throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
            }

        } catch (RestClientException e) {
            log.error("[Expo] 배치 푸시 전송 실패 - error: {}", e.getMessage(), e);
            throw new GeneralException(PushErrorCode.PUSH_SEND_FAILED);
        }
    }
}
