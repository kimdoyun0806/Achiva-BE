package unicon.Achiva.domain.cheering;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CheeringCategory {
    PRAISE("최고예요"),
    APPRECIATION("수고했어요"),
    ENCOURAGEMENT("응원해요"),
    MOTIVATION("동기부여");

    private final String description;

    CheeringCategory(String description) {
        this.description = description;
    }

    @JsonCreator
    public static CheeringCategory fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(name) || c.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리: " + name));
    }

    public static String getDisplayName(CheeringCategory category) {
        return category.getDescription();
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
