package kr.eolmago.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * QueryDSL 설정
 *
 * 역할:
 * - JPAQueryFactory 빈 생성
 * - EntityManager를 QueryDSL에서 사용할 수 있도록 설정
 *
 * 필요한 이유:
 * - Repository의 Custom 구현체에서 QueryDSL 사용
 * - JPQL 문자열 대신 타입 안전한 쿼리 작성
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory 빈 생성
     *
     * JPAQueryFactory란?:
     * - QueryDSL에서 JPQL 쿼리를 생성하는 핵심 클래스
     * - EntityManager를 기반으로 쿼리 실행
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
