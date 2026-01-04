package kr.eolmago.domain.entity.search.enums;

/**
 * 검색어 타입
 * - 브랜드 키워드에 가중치 +100 부여
 * - 검색 결과 정렬 시 브랜드 키워드 우선 노출
 *
 * 연결 부분:
 * - SearchKeyword 엔티티에서 사용
 * - 자동완성 점수 계산 시 브랜드 여부 판단에 사용
 */
public enum KeywordType {

    /**
     * 브랜드 키워드 (예: "아이폰", "갤럭시", "Apple", "Samsung")
     * 자동완성 점수 계산 시 +100 가중치 부여
     */
    BRAND,

    /**
     * 모델명 키워드 (예: "아이폰 14", "갤럭시 S23")
     * 숫자가 포함된 키워드
     */
    MODEL,

    /**
     * 일반 키워드 (예: "중고폰", "새제품")
     */
    GENERAL
}

