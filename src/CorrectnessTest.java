
import java.util.*;

public class CorrectnessTest {

    private static void check(
            String name,
            String text,
            List<String> patterns) {

        AhoCorasick ac = new AhoCorasick();

        for (String pattern : patterns) {
            ac.addPattern(pattern);
        }

        ac.build();

        List<AhoCorasick.Match> matches = ac.search(text);
        Map<Integer, List<Integer>> actual = new HashMap<>();

        for (AhoCorasick.Match match : matches) {
            actual.computeIfAbsent(
                    match.patternId,
                    key -> new ArrayList<>()
            ).add(match.start);
        }

        for (int id = 0; id < patterns.size(); id++) {
            List<Integer> expected =
                    KMPSearch.search(text, patterns.get(id));

            List<Integer> found =
                    actual.getOrDefault(id, new ArrayList<>());

            Collections.sort(found);

            if (!expected.equals(found)) {
                throw new AssertionError(
                        name + " FAILED for pattern: "
                                + patterns.get(id)
                                + "\nExpected: " + expected
                                + "\nActual: " + found);
            }
        }

        System.out.println("PASS: " + name
                + " | matches = " + matches.size());
    }

    public static void main(String[] args) {

        check(
                "Classic example",
                "ushers",
                Arrays.asList("he", "she", "his", "hers")
        );

        check(
                "Overlapping matches",
                "aaaaa",
                Arrays.asList("a", "aa", "aaa")
        );

        check(
                "No matches",
                "abcdef",
                Arrays.asList("xyz", "hello")
        );

        check(
                "Repeated patterns",
                "banana",
                Arrays.asList("ana", "ana", "na")
        );

        check(
                "Mixed characters",
                "A1!a1!A1!",
                Arrays.asList("A1!", "a1!", "1!")
        );

        check(
                "Patterns longer than text",
                "abc",
                Arrays.asList("abcdef", "bc")
        );

        System.out.println("ALL TESTS PASSED!");
    }
}
