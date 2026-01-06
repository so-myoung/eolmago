package kr.eolmago.notification.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import kr.eolmago.domain.entity.notification.Notification;
import kr.eolmago.repository.notification.NotificationRepository;

final class NotificationCommandServiceTestDoubles {

	final NotificationRepository notificationRepository;

	private final Map<Key, Notification> store = new HashMap<>();
	private int markAllReadReturn = 0;

	private NotificationCommandServiceTestDoubles() {
		this.notificationRepository = mock(NotificationRepository.class);
		wire();
	}

	static NotificationCommandServiceTestDoubles create() {
		return new NotificationCommandServiceTestDoubles();
	}

	void put(UUID userId, Long notificationId, Notification notification) {
		store.put(new Key(userId, notificationId), notification);
	}

	void markAllReadReturns(int count) {
		this.markAllReadReturn = count;
	}

	private void wire() {
		when(notificationRepository.findByNotificationIdAndUser_UserIdAndDeletedFalse(anyLong(), any(UUID.class)))
			.thenAnswer(inv -> {
				Long notificationId = inv.getArgument(0);
				UUID userId = inv.getArgument(1);
				return Optional.ofNullable(store.get(new Key(userId, notificationId)));
			});

		when(notificationRepository.markAllRead(any(UUID.class), any(OffsetDateTime.class)))
			.thenAnswer(inv -> markAllReadReturn);
	}

	private record Key(UUID userId, Long notificationId) {}
}
