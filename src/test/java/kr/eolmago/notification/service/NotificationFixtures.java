package kr.eolmago.notification.service;

import static org.mockito.Mockito.*;

import kr.eolmago.domain.entity.notification.Notification;

final class NotificationFixtures {

	private NotificationFixtures() {}

	static Notification notification() {
		return mock(Notification.class);
	}
}
