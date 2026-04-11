package unicon.Achiva.global.utill;

import java.util.function.Predicate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for normalizing and tagging nicknames in the form of "name#1234".
 */
public final class NicknameTagUtil {

    private static final int DEFAULT_TAG_DIGITS = 4;
    private static final int ATTEMPTS_PER_DIGIT_LENGTH = 100;
    private static final Pattern TAGGED_NICKNAME_PATTERN = Pattern.compile("^(.*)#(\\d+)$");

    private NicknameTagUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String extractBaseNickname(String nickname) {
        if (nickname == null) {
            return null;
        }

        String trimmedNickname = nickname.trim();
        if (trimmedNickname.isBlank()) {
            return null;
        }

        Matcher matcher = TAGGED_NICKNAME_PATTERN.matcher(trimmedNickname);
        if (matcher.matches()) {
            String baseNickname = matcher.group(1).trim();
            return baseNickname.isBlank() ? null : baseNickname;
        }

        return trimmedNickname;
    }

    public static String generateUniqueTaggedNickname(String baseNickname, Predicate<String> existsChecker) {
        String normalizedBaseNickname = extractBaseNickname(baseNickname);
        if (normalizedBaseNickname == null) {
            throw new IllegalArgumentException("Nickname must not be blank");
        }

        int digits = DEFAULT_TAG_DIGITS;
        while (true) {
            for (int attempt = 0; attempt < ATTEMPTS_PER_DIGIT_LENGTH; attempt++) {
                String candidate = normalizedBaseNickname + "#" + generateDigits(digits);
                if (!existsChecker.test(candidate)) {
                    return candidate;
                }
            }
            digits++;
        }
    }

    private static String generateDigits(int digits) {
        StringBuilder builder = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            builder.append(ThreadLocalRandom.current().nextInt(10));
        }
        return builder.toString();
    }
}
