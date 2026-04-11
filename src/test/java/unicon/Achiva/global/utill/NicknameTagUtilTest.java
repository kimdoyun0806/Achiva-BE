package unicon.Achiva.global.utill;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NicknameTagUtilTest {

    @Test
    void extractBaseNicknameRemovesGeneratedTagSuffix() {
        assertThat(NicknameTagUtil.extractBaseNickname("홍길동#1234")).isEqualTo("홍길동");
        assertThat(NicknameTagUtil.extractBaseNickname("홍길동")).isEqualTo("홍길동");
    }

    @Test
    void generateUniqueTaggedNicknameStartsWithFourDigitTag() {
        String nickname = NicknameTagUtil.generateUniqueTaggedNickname("홍길동", candidate -> false);

        assertThat(nickname).matches("홍길동#\\d{4}");
    }

    @Test
    void generateUniqueTaggedNicknameExpandsTagLengthWhenShorterTagsAreSaturated() {
        String nickname = NicknameTagUtil.generateUniqueTaggedNickname(
                "홍길동#1234",
                candidate -> candidate.matches("홍길동#\\d{4}")
        );

        assertThat(nickname).matches("홍길동#\\d{5}");
    }
}
