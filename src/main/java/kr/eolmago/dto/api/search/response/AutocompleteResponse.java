package kr.eolmago.dto.api.search.response;

import kr.eolmago.domain.entity.search.SearchKeyword;

/**
 * 자동완성 API 응답 DTO
 *  - 검색어 + 검색 횟수 반환
 *
 * 사용 예시:
 * GET /api/search/autocomplete?q=아이
 *
 * Response:
 * [
 *   { "keyword": "아이폰", "searchCount": 1247 },
 *   { "keyword": "아이폰 14", "searchCount": 856 }
 * ]
 */
public record AutocompleteResponse(
        String keyword, // 검색어
        Integer searchCount // 검색 횟수
) {
    /**
     * SearchKeyword Entity → DTO 변환
     *
     * 호출 시점:
     * - DB 조회 결과 변환
     * - Redis Fallback 시 사용
     *
     * @param searchKeyword SearchKeyword 엔티티
     * @return AutocompleteResponse
     */
    public static AutocompleteResponse from(SearchKeyword searchKeyword) {
        return new AutocompleteResponse(
                searchKeyword.getKeyword(),
                searchKeyword.getSearchCount()
        );
    }

    /**
     * 키워드 + 점수로 DTO 생성
     *
     * 호출 시점:
     * - Redis에서 자동완성 조회 시
     * - (keyword, score) 튜플 → DTO
     *
     * @param keyword 검색어
     * @param score Redis Sorted Set 점수
     * @return AutocompleteResponse
     */
    public static AutocompleteResponse of(String keyword, Double score) {
        return new AutocompleteResponse(
                keyword,
                score.intValue()
        );
    }
}
