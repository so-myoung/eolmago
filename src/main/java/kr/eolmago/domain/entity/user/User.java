package kr.eolmago.domain.entity.user;

import jakarta.persistence.*;
import kr.eolmago.domain.entity.common.AuditableEntity;
import kr.eolmago.domain.entity.user.enums.UserRole;
import kr.eolmago.domain.entity.user.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    public static User create(
            UserRole role
    ) {
        User user = new User();
        user.role = role;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    public void changeRole(User admin, UserRole newRole) {
        if (admin.role != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (newRole == null) {
            throw new IllegalArgumentException("변경할 역할은 null이 될 수 없습니다.");
        }

        if (this.role == newRole) {
            throw new IllegalArgumentException("현재 역할과 동일한 역할로 변경할 수 없습니다.");
        }

        this.role = newRole;
    }

    public void changeStatus(User admin, UserStatus newStatus) {
        if (admin.role != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (newStatus == null) {
            throw new IllegalArgumentException("변경할 상태는 null이 될 수 없습니다.");
        }

        if (this.status == newStatus) {
            throw new IllegalArgumentException("현재 상태와 동일한 상태로 변경할 수 없습니다.");
        }

        this.status = newStatus;
    }

    // ToDo 정지 기간 설정 스케쥴러 활용 예정
    public void suspend(User admin) {
        if (admin.role != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalArgumentException("이미 정지된 사용자입니다.");
        }

        this.status = UserStatus.SUSPENDED;
    }

    public void ban(User admin) {
        if (admin.role != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (this.status == UserStatus.BANNED) {
            throw new IllegalArgumentException("이미 차단된 사용자입니다.");
        }

        this.status = UserStatus.BANNED;
    }

    public void activate(User admin) {
        if (admin.role != UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정이 아닙니다.");
        }

        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 활성화된 사용자입니다.");
        }

        this.status = UserStatus.ACTIVE;
    }

    public void autoActivate() {
        if (this.status == UserStatus.ACTIVE) {
            return;  // 이미 활성화된 경우 무시
        }
        this.status = UserStatus.ACTIVE;
    }
}