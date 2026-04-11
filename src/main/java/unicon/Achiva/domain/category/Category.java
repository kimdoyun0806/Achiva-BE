package unicon.Achiva.domain.category;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Category {
    GRACE("오늘 은혜"),
    BIBLE("성경 일독"),
    ALBUM("교회 앨범");

    private final String description;

    Category(String description) {
        this.description = description;
    }

    @JsonCreator
    public static Category fromDisplayName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.description.equals(name) || c.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 카테고리: " + name));
    }

    public static String getDisplayName(Category category) {
        return category.getDescription();
    }

    @JsonValue
    public String toValue() {
        return this.description;
    }
}
