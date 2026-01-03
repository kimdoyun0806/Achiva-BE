package unicon.Achiva.domain.category;

import java.util.List;

public record CategoryCountResponse(List<CategoryCount> categoryCounts) {

    public static CategoryCountResponse fromObjectList(List<Object[]> categoryCountObjects) {
        List<CategoryCount> categoryCounts = categoryCountObjects.stream()
                .map(obj -> new CategoryCount((String) obj[0], (Long) obj[1]))
                .toList();
        return new CategoryCountResponse(categoryCounts);
    }


    public record CategoryCount(String category, Long count) {
    }
}
