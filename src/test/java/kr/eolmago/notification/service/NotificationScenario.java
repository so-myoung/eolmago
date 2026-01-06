package kr.eolmago.notification.service;

import java.util.UUID;

import kr.eolmago.domain.entity.notification.Notification;

final class NotificationScenario {

	private final NotificationCommandServiceTestDoubles doubles;

	final UUID userId = UUID.randomUUID();
	final Long notificationId = 1L;

	final Notification notification = NotificationFixtures.notification();

	private NotificationScenario(NotificationCommandServiceTestDoubles doubles) {
		this.doubles = doubles;
	}

	static NotificationScenario given(NotificationCommandServiceTestDoubles doubles) {
		return new NotificationScenario(doubles);
	}

	NotificationScenario notificationExists() {
		doubles.put(userId, notificationId, notification);
		return this;
	}

	NotificationScenario markAllReadReturns(int count) {
		doubles.markAllReadReturns(count);
		return this;
	}
}
