package kr.eolmago.notification.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;

import kr.eolmago.service.notification.NotificationCommandService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class NotificationCommandServiceTest {

	private NotificationCommandServiceTestDoubles doubles;
	private NotificationCommandService sut;

	@BeforeEach
	void setUp() {
		doubles = NotificationCommandServiceTestDoubles.create();
		sut = new NotificationCommandService(doubles.notificationRepository);
	}

	private NotificationScenario given() {
		return NotificationScenario.given(doubles);
	}

	@Test
	@DisplayName("알림 1건 읽음 처리: 존재하면 readAt 설정")
	void givenNotificationExists_whenReadOne_thenMarkRead() {
		// Given
		NotificationScenario s = given().notificationExists();

		// When
		sut.readOne(s.userId, s.notificationId);

		// Then
		ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
		verify(s.notification).markRead(captor.capture());
		assertThat(captor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("알림 1건 읽음 처리: 없으면 404")
	void givenNotificationMissing_whenReadOne_thenThrow404() {
		// Given
		NotificationScenario s = given();

		// When
		Throwable t = catchThrowable(() -> sut.readOne(s.userId, s.notificationId));

		// Then
		assertThat(t).isInstanceOf(ResponseStatusException.class);
		ResponseStatusException ex = (ResponseStatusException) t;
		assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ex.getReason()).contains("알림을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("알림 삭제: 존재하면 soft delete 처리")
	void givenNotificationExists_whenDelete_thenSoftDelete() {
		// Given
		NotificationScenario s = given().notificationExists();

		// When
		sut.delete(s.userId, s.notificationId);

		// Then
		ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
		verify(s.notification).softDelete(captor.capture());
		assertThat(captor.getValue()).isNotNull();
	}

	@Test
	@DisplayName("알림 삭제: 없으면 404")
	void givenNotificationMissing_whenDelete_thenThrow404() {
		// Given
		NotificationScenario s = given();

		// When
		Throwable t = catchThrowable(() -> sut.delete(s.userId, s.notificationId));

		// Then
		assertThat(t).isInstanceOf(ResponseStatusException.class);
		ResponseStatusException ex = (ResponseStatusException) t;
		assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ex.getReason()).contains("알림을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("알림 전체 읽음: repo 결과를 그대로 반환")
	void givenMarkAllReadReturn_whenReadAll_thenReturnCount() {
		// Given
		NotificationScenario s = given().markAllReadReturns(7);

		// When
		int count = sut.readAll(s.userId);

		// Then
		assertThat(count).isEqualTo(7);
		verify(doubles.notificationRepository).markAllRead(eq(s.userId), any(OffsetDateTime.class));
	}
}
