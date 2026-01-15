package unicon.Achiva.domain.category.dto;

import java.util.List;

public record CategoryGroupResponse(
        String groupName,
        List<CategoryItem> items
) {
    public record CategoryItem(String code, String displayName) {}

    public static List<CategoryGroupResponse> getAllGroups() {
        return List.of(
                new CategoryGroupResponse("근력/트레이닝", List.of(
                        new CategoryItem("GYM", "헬스"),
                        new CategoryItem("BODYWEIGHT", "맨몸운동"),
                        new CategoryItem("CROSSFIT", "크로스핏")
                )),
                new CategoryGroupResponse("유산소", List.of(
                        new CategoryItem("RUNNING", "러닝"),
                        new CategoryItem("WALKING", "걷기"),
                        new CategoryItem("CYCLING", "사이클"),
                        new CategoryItem("HIKING", "등산")
                )),
                new CategoryGroupResponse("구기종목", List.of(
                        new CategoryItem("SOCCER", "축구"),
                        new CategoryItem("BASKETBALL", "농구"),
                        new CategoryItem("FUTSAL", "풋살"),
                        new CategoryItem("BASEBALL", "야구"),
                        new CategoryItem("BOWLING", "볼링"),
                        new CategoryItem("GOLF", "골프"),
                        new CategoryItem("VOLLEYBALL", "배구"),
                        new CategoryItem("RUGBY", "럭비")
                )),
                new CategoryGroupResponse("라켓/스틱", List.of(
                        new CategoryItem("TABLE_TENNIS", "탁구"),
                        new CategoryItem("BADMINTON", "배드민턴"),
                        new CategoryItem("TENNIS", "테니스"),
                        new CategoryItem("HOCKEY", "하키")
                )),
                new CategoryGroupResponse("격투/무술", List.of(
                        new CategoryItem("BOXING", "복싱/격복싱"),
                        new CategoryItem("MMA", "MMA"),
                        new CategoryItem("JUDO", "유도"),
                        new CategoryItem("TAEKWONDO", "태권도"),
                        new CategoryItem("JIU_JITSU", "주짓수"),
                        new CategoryItem("WRESTLING", "레슬링"),
                        new CategoryItem("FENCING", "검도/펜싱")
                )),
                new CategoryGroupResponse("수상", List.of(
                        new CategoryItem("SWIMMING", "수영"),
                        new CategoryItem("SURFING", "서핑"),
                        new CategoryItem("ROWING", "조정")
                )),
                new CategoryGroupResponse("자세", List.of(
                        new CategoryItem("YOGA", "요가"),
                        new CategoryItem("PILATES", "필라테스"),
                        new CategoryItem("REHABILITATION", "교정/재활"),
                        new CategoryItem("STRETCHING", "스트레칭")
                )),
                new CategoryGroupResponse("등반", List.of(
                        new CategoryItem("CLIMBING", "클라이밍")
                )),
                new CategoryGroupResponse("스케이트/스키", List.of(
                        new CategoryItem("INLINE_SKATING", "롤러/인라인"),
                        new CategoryItem("SKATEBOARD", "보드"),
                        new CategoryItem("ICE_SKATING", "빙상"),
                        new CategoryItem("SKIING", "스키/보드")
                ))
        );
    }
}
