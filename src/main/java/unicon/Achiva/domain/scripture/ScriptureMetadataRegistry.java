package unicon.Achiva.domain.scripture;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ScriptureMetadataRegistry {

    private static final List<ScriptureMetadata> SCRIPTURES = List.of(
            new ScriptureMetadata("창세기", "창세기", Testament.OLD, 50, 1),
            new ScriptureMetadata("출애굽기", "출애굽기", Testament.OLD, 40, 2),
            new ScriptureMetadata("레위기", "레위기", Testament.OLD, 27, 3),
            new ScriptureMetadata("민수기", "민수기", Testament.OLD, 36, 4),
            new ScriptureMetadata("신명기", "신명기", Testament.OLD, 34, 5),
            new ScriptureMetadata("여호수아", "여호수아", Testament.OLD, 24, 6),
            new ScriptureMetadata("사사기", "사사기", Testament.OLD, 21, 7),
            new ScriptureMetadata("룻기", "룻기", Testament.OLD, 4, 8),
            new ScriptureMetadata("사무엘상", "사무엘상", Testament.OLD, 31, 9),
            new ScriptureMetadata("사무엘하", "사무엘하", Testament.OLD, 24, 10),
            new ScriptureMetadata("열왕기상", "열왕기상", Testament.OLD, 22, 11),
            new ScriptureMetadata("열왕기하", "열왕기하", Testament.OLD, 25, 12),
            new ScriptureMetadata("역대상", "역대상", Testament.OLD, 29, 13),
            new ScriptureMetadata("역대하", "역대하", Testament.OLD, 36, 14),
            new ScriptureMetadata("에스라", "에스라", Testament.OLD, 10, 15),
            new ScriptureMetadata("느헤미야", "느헤미야", Testament.OLD, 13, 16),
            new ScriptureMetadata("에스더", "에스더", Testament.OLD, 10, 17),
            new ScriptureMetadata("욥기", "욥기", Testament.OLD, 42, 18),
            new ScriptureMetadata("시편", "시편", Testament.OLD, 150, 19),
            new ScriptureMetadata("잠언", "잠언", Testament.OLD, 31, 20),
            new ScriptureMetadata("전도서", "전도서", Testament.OLD, 12, 21),
            new ScriptureMetadata("아가", "아가", Testament.OLD, 8, 22),
            new ScriptureMetadata("이사야", "이사야", Testament.OLD, 66, 23),
            new ScriptureMetadata("예레미야", "예레미야", Testament.OLD, 52, 24),
            new ScriptureMetadata("예레미야애가", "예레미야애가", Testament.OLD, 5, 25),
            new ScriptureMetadata("에스겔", "에스겔", Testament.OLD, 48, 26),
            new ScriptureMetadata("다니엘", "다니엘", Testament.OLD, 12, 27),
            new ScriptureMetadata("호세아", "호세아", Testament.OLD, 14, 28),
            new ScriptureMetadata("요엘", "요엘", Testament.OLD, 3, 29),
            new ScriptureMetadata("아모스", "아모스", Testament.OLD, 9, 30),
            new ScriptureMetadata("오바댜", "오바댜", Testament.OLD, 1, 31),
            new ScriptureMetadata("요나", "요나", Testament.OLD, 4, 32),
            new ScriptureMetadata("미가", "미가", Testament.OLD, 7, 33),
            new ScriptureMetadata("나훔", "나훔", Testament.OLD, 3, 34),
            new ScriptureMetadata("하박국", "하박국", Testament.OLD, 3, 35),
            new ScriptureMetadata("스바냐", "스바냐", Testament.OLD, 3, 36),
            new ScriptureMetadata("학개", "학개", Testament.OLD, 2, 37),
            new ScriptureMetadata("스가랴", "스가랴", Testament.OLD, 14, 38),
            new ScriptureMetadata("말라기", "말라기", Testament.OLD, 4, 39),
            new ScriptureMetadata("마태복음", "마태복음", Testament.NEW, 28, 40),
            new ScriptureMetadata("마가복음", "마가복음", Testament.NEW, 16, 41),
            new ScriptureMetadata("누가복음", "누가복음", Testament.NEW, 24, 42),
            new ScriptureMetadata("요한복음", "요한복음", Testament.NEW, 21, 43),
            new ScriptureMetadata("사도행전", "사도행전", Testament.NEW, 28, 44),
            new ScriptureMetadata("로마서", "로마서", Testament.NEW, 16, 45),
            new ScriptureMetadata("고린도전서", "고린도전서", Testament.NEW, 16, 46),
            new ScriptureMetadata("고린도후서", "고린도후서", Testament.NEW, 13, 47),
            new ScriptureMetadata("갈라디아서", "갈라디아서", Testament.NEW, 6, 48),
            new ScriptureMetadata("에베소서", "에베소서", Testament.NEW, 6, 49),
            new ScriptureMetadata("빌립보서", "빌립보서", Testament.NEW, 4, 50),
            new ScriptureMetadata("골로새서", "골로새서", Testament.NEW, 4, 51),
            new ScriptureMetadata("데살로니가전서", "데살로니가전서", Testament.NEW, 5, 52),
            new ScriptureMetadata("데살로니가후서", "데살로니가후서", Testament.NEW, 3, 53),
            new ScriptureMetadata("디모데전서", "디모데전서", Testament.NEW, 6, 54),
            new ScriptureMetadata("디모데후서", "디모데후서", Testament.NEW, 4, 55),
            new ScriptureMetadata("디도서", "디도서", Testament.NEW, 3, 56),
            new ScriptureMetadata("빌레몬서", "빌레몬서", Testament.NEW, 1, 57),
            new ScriptureMetadata("히브리서", "히브리서", Testament.NEW, 13, 58),
            new ScriptureMetadata("야고보서", "야고보서", Testament.NEW, 5, 59),
            new ScriptureMetadata("베드로전서", "베드로전서", Testament.NEW, 5, 60),
            new ScriptureMetadata("베드로후서", "베드로후서", Testament.NEW, 3, 61),
            new ScriptureMetadata("요한일서", "요한일서", Testament.NEW, 5, 62),
            new ScriptureMetadata("요한이서", "요한이서", Testament.NEW, 1, 63),
            new ScriptureMetadata("요한삼서", "요한삼서", Testament.NEW, 1, 64),
            new ScriptureMetadata("유다서", "유다서", Testament.NEW, 1, 65),
            new ScriptureMetadata("요한계시록", "요한계시록", Testament.NEW, 22, 66)
    );

    private static final Map<String, ScriptureMetadata> SCRIPTURE_MAP = SCRIPTURES.stream()
            .collect(Collectors.toUnmodifiableMap(ScriptureMetadata::id, Function.identity()));

    private ScriptureMetadataRegistry() {
    }

    public static boolean contains(String scriptureId) {
        return SCRIPTURE_MAP.containsKey(scriptureId);
    }

    public static ScriptureMetadata get(String scriptureId) {
        return SCRIPTURE_MAP.get(scriptureId);
    }

    public static int totalChaptersOf(String scriptureId) {
        ScriptureMetadata metadata = get(scriptureId);
        return metadata == null ? -1 : metadata.totalChapters();
    }

    public enum Testament {
        OLD,
        NEW
    }

    public record ScriptureMetadata(
            String id,
            String name,
            Testament testament,
            int totalChapters,
            int displayOrder
    ) {
    }
}
