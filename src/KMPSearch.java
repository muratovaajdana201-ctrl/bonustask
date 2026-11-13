
import java.util.*;

public class KMPSearch {

    private static int[] computeLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            } else if (length != 0) {
                length = lps[length - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    public static List<Integer> search(
            String text, String pattern) {

        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException(
                    "Pattern must not be empty");
        }

        List<Integer> positions = new ArrayList<>();
        int[] lps = computeLPS(pattern);

        int i = 0;
        int j = 0;

        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == pattern.length()) {
                positions.add(i - j);
                j = lps[j - 1];
            } else if (i < text.length()
                    && text.charAt(i) != pattern.charAt(j)) {

                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return positions;
    }

    public static long countAll(
            String text, List<String> patterns) {

        long total = 0;

        for (String pattern : patterns) {
            total += countOne(text, pattern);
        }

        return total;
    }

    private static long countOne(String text, String pattern) {
        int[] lps = computeLPS(pattern);

        int i = 0;
        int j = 0;
        long count = 0;

        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == pattern.length()) {
                count++;
                j = lps[j - 1];
            } else if (i < text.length()
                    && text.charAt(i) != pattern.charAt(j)) {

                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return count;
    }
}
