package kr.eolmago.service.notification;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kr.eolmago.dto.api.notification.response.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class NotificationSseRegistry {

	private static final long DEFAULT_TIMEOUT_MILLIS = 60L * 60 * 1000; // 1 hour
	private static final long DEFAULT_RECONNECT_TIME_MILLIS = 3000L;

	private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters;

	public NotificationSseRegistry() {
		this.emitters = new ConcurrentHashMap<>();
	}

	public SseEmitter connect(UUID userId) {
		SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);

		CopyOnWriteArrayList<SseEmitter> list =
			emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
		list.add(emitter);

		emitter.onCompletion(() -> remove(userId, emitter));
		emitter.onTimeout(() -> remove(userId, emitter));
		emitter.onError(e -> remove(userId, emitter));

		// 최초 연결 이벤트(프록시/브라우저에서 연결 유지 도움)
		try {
			emitter.send(SseEmitter.event()
				.name("INIT")
				.data("connected")
				.reconnectTime(DEFAULT_RECONNECT_TIME_MILLIS));
		} catch (IOException e) {
			remove(userId, emitter);
		}

		return emitter;
	}

	public void push(UUID userId, NotificationResponse data) {
		CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
		if (list == null || list.isEmpty()) {
			return;
		}

		for (SseEmitter emitter : list) {
			try {
				emitter.send(SseEmitter.event()
					.name("NOTIFICATION")
					.data(data, MediaType.APPLICATION_JSON));
			} catch (IOException e) {
				remove(userId, emitter);
			}
		}
	}

	private void remove(UUID userId, SseEmitter emitter) {
		CopyOnWriteArrayList<SseEmitter> list = emitters.get(userId);
		if (list == null) {
			return;
		}

		list.remove(emitter);

		if (list.isEmpty()) {
			emitters.remove(userId);
		}
	}
}
