package unicon.Achiva.domain.cheering.infrastructure;

import unicon.Achiva.domain.cheering.CheeringCategory;
import unicon.Achiva.domain.cheering.dto.CategoryStatDto;

/**
 * 카테고리별 응원 통계 프로젝션.
 * JPQL alias: category, count, points
 */
public interface CategoryStatProjection {
    CheeringCategory getCheeringCategory();

    long getCount();

    long getPoints();

    /**
     * Projection 데이터를 DTO로 변환합니다.
     *
     * @return CategoryStatDto 인스턴스
     */
    default CategoryStatDto toDto() {
        return new CategoryStatDto(
                CheeringCategory.getDisplayName(getCheeringCategory()),
                getCount(),
                getPoints()
        );
    }
}