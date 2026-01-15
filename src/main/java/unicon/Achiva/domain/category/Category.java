package unicon.Achiva.domain.category;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Category {
    // 근력/트레이닝 (3)
    GYM("헬스"),
    BODYWEIGHT("맨몸운동"),
    CROSSFIT("크로스핏"),

    // 유산소 (4)
    RUNNING("러닝"),
    WALKING("걷기"),
    CYCLING("사이클"),
    HIKING("등산"),

    // 구기종목 (8)
    SOCCER("축구"),
    BASKETBALL("농구"),
    FUTSAL("풋살"),
    BASEBALL("야구"),
    BOWLING("볼링"),
    GOLF("골프"),
    VOLLEYBALL("배구"),
    RUGBY("럭비"),

    // 라켓/스틱 (4)
    TABLE_TENNIS("탁구"),
    BADMINTON("배드민턴"),
    TENNIS("테니스"),
    HOCKEY("하키"),

    // 격투/무술 (7)
    BOXING("복싱/격복싱"),
    MMA("MMA"),
    JUDO("유도"),
    TAEKWONDO("태권도"),
    JIU_JITSU("주짓수"),
    WRESTLING("레슬링"),
    FENCING("검도/펜싱"),

    // 수상 (3)
    SWIMMING("수영"),
    SURFING("서핑"),
    ROWING("조정"),

    // 자세 (4)
    YOGA("요가"),
    PILATES("필라테스"),
    REHABILITATION("교정/재활"),
    STRETCHING("스트레칭"),

    // 등반 (1)
    CLIMBING("클라이밍"),

    // 스케이트/스키 (4)
    INLINE_SKATING("롤러/인라인"),
    SKATEBOARD("보드"),
    ICE_SKATING("빙상"),
    SKIING("스키/보드");

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
