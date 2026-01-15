package kr.eolmago.repository.user.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.eolmago.domain.entity.user.QSocialLogin;
import kr.eolmago.domain.entity.user.QUser;
import kr.eolmago.domain.entity.user.QUserProfile;
import kr.eolmago.domain.entity.user.User;
import kr.eolmago.domain.entity.user.enums.UserStatus;
import kr.eolmago.repository.user.UserRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> findUsersWithFilters(String name, String email, UserStatus status, Pageable pageable) {
        QUser user = QUser.user;
        QUserProfile profile = QUserProfile.userProfile;
        QSocialLogin socialLogin = QSocialLogin.socialLogin;

        BooleanBuilder builder = new BooleanBuilder();

        // 동적 쿼리 조건
        if (name != null && !name.isBlank()) {
            builder.and(profile.nickname.containsIgnoreCase(name));
        }

        if (email != null && !email.isBlank()) {
            builder.and(socialLogin.email.containsIgnoreCase(email));
        }

        if (status != null) {
            builder.and(user.status.eq(status));
        }

        // 데이터 조회 (fetchJoin으로 N+1 문제 방지)
        List<User> users = queryFactory
                .selectFrom(user)
                .distinct()
                .leftJoin(user.userProfile, profile).fetchJoin()
                .leftJoin(socialLogin).on(socialLogin.user.eq(user))
                .where(builder)
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 개수 조회
        Long total = queryFactory
                .select(user.countDistinct())
                .from(user)
                .leftJoin(profile).on(profile.user.eq(user))
                .leftJoin(socialLogin).on(socialLogin.user.eq(user))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(users, pageable, total != null ? total : 0);
    }

    @Override
    public Optional<String> findEmailById(UUID userId) {
        QSocialLogin socialLogin = QSocialLogin.socialLogin;

        String email = queryFactory
                .select(socialLogin.email)
                .from(socialLogin)
                .where(socialLogin.user.userId.eq(userId))
                .fetchFirst(); // 다건 가능성 대비 (fetchOne 대신)

        return Optional.ofNullable(email);
    }
}
