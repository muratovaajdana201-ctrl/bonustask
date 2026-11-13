
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String text = "ushers";

        List<String> patterns = Arrays.asList(
                "he", "she", "his", "hers"
        );

        AhoCorasick ac = new AhoCorasick();

        for (String pattern : patterns) {
            ac.addPattern(pattern);
        }

        ac.build();

        List<AhoCorasick.Match> matches = ac.search(text);

        System.out.println("Text: " + text);
        System.out.println("Patterns: " + patterns);
        System.out.println("\nAho-Corasick results:");

        for (AhoCorasick.Match match : matches) {
            System.out.println(
                    "Pattern: " + ac.getPattern(match.patternId)
                            + " | Start: " + match.start
                            + " | End: " + match.end
            );
        }

        System.out.println(
                "\nTotal matches: " + matches.size()
        );
    }
}
