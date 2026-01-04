package kr.eolmago.dto.api.search.response;

import kr.eolmago.domain.entity.search.SearchKeyword;

/**
 * 인기 검색어 응답 DTO
 *  - 인기 검색어 Top 10 응답
 *
 * 사용 예시:
 * GET /api/search/popular
 *
 * Response:
 * [
 *   { "rank": 1, "keyword": "아이폰", "searchCount": 1247 },
 *   { "rank": 2, "keyword": "갤럭시", "searchCount": 1134 }
 * ]
 */
public record PopularKeywordResponse(
        Integer rank, // 순위 (1~10)
        String keyword, // 검색어
        Integer searchCount // 검색 횟수
) {
    /**
     * SearchKeyword + 순위 → DTO 변환
     *
     * @param searchKeyword SearchKeyword 엔티티
     * @param rank 순위 (1부터 시작)
     * @return PopularKeywordResponse
     */
    public static PopularKeywordResponse of(SearchKeyword searchKeyword, int rank) {
        return new PopularKeywordResponse(
                rank,
                searchKeyword.getKeyword(),
                searchKeyword.getSearchCount()
        );
    }
}
