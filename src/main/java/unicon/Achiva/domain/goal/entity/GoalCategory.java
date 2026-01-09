package unicon.Achiva.domain.goal.entity;

import lombok.Getter;

@Getter
public enum GoalCategory {
    VISION("운동 목표"),
    MISSION("나의 미션"),
    MINDSET("마음가짐");

    private final String displayName;

    GoalCategory(String displayName) {
        this.displayName = displayName;
    }

    public static GoalCategory fromDisplayName(String displayName) {
        for (GoalCategory category : GoalCategory.values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown GoalCategory: " + displayName);
    }

    public static String getDisplayName(GoalCategory category) {
        return category.displayName;
    }
}
