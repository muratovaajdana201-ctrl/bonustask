
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Benchmark {

    private static final int[] PATTERN_COUNTS = {
            10, 100, 1000
    };

    private static final int[] TEXT_LENGTHS = {
            1000, 10000, 100000
    };

    private static final int RUNS = 5;
    private static final int PATTERN_LENGTH = 8;
    private static final String ALPHABET = "abcd";

    private static volatile long blackhole = 0;

    private static String randomString(
            Random random, int length) {

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(
                    random.nextInt(ALPHABET.length())));
        }

        return sb.toString();
    }

    private static List<String> generatePatterns(
            Random random, int count) {

        Set<String> unique = new LinkedHashSet<>();

        while (unique.size() < count) {
            unique.add(randomString(
                    random, PATTERN_LENGTH));
        }

        return new ArrayList<>(unique);
    }

    private static AhoCorasick buildAutomaton(
            List<String> patterns) {

        AhoCorasick ac = new AhoCorasick();

        for (String pattern : patterns) {
            ac.addPattern(pattern);
        }

        ac.build();
        return ac;
    }

    private static double average(long[] times) {
        long sum = 0;

        for (long time : times) {
            sum += time;
        }

        return sum / (double) times.length / 1_000_000.0;
    }

    public static void main(String[] args)
            throws IOException {

        StringBuilder csv = new StringBuilder();

        csv.append(
                "patterns,text_length,ac_build_ms,"
                        + "ac_search_ms,kmp_search_ms,matches\n"
        );

        System.out.printf(
                "%-10s %-12s %-14s %-14s %-14s %-10s%n",
                "Patterns", "Text", "AC Build",
                "AC Search", "KMP Search", "Matches"
        );

        for (int patternCount : PATTERN_COUNTS) {
            for (int textLength : TEXT_LENGTHS) {

                Random random = new Random(
                        42L + patternCount * 100000L
                                + textLength
                );

                String text = randomString(
                        random, textLength);

                List<String> patterns = generatePatterns(
                        random, patternCount);

                // Warm up the JVM before measurements.
                for (int w = 0; w < 3; w++) {
                    AhoCorasick warmup =
                            buildAutomaton(patterns);

                    blackhole = warmup.countMatches(text);
                    blackhole = KMPSearch.countAll(
                            text, patterns);
                }

                long[] buildTimes = new long[RUNS];
                long[] acTimes = new long[RUNS];
                long[] kmpTimes = new long[RUNS];

                long matchCount = 0;

                for (int run = 0; run < RUNS; run++) {

                    long start = System.nanoTime();

                    AhoCorasick ac =
                            buildAutomaton(patterns);

                    long afterBuild = System.nanoTime();

                    long acCount = ac.countMatches(text);

                    long afterAC = System.nanoTime();

                    long kmpCount = KMPSearch.countAll(
                            text, patterns);

                    long afterKMP = System.nanoTime();

                    if (acCount != kmpCount) {
                        throw new AssertionError(
                                "Different match counts!");
                    }

                    buildTimes[run] =
                            afterBuild - start;

                    acTimes[run] =
                            afterAC - afterBuild;

                    kmpTimes[run] =
                            afterKMP - afterAC;

                    matchCount = acCount;
                    blackhole = acCount + kmpCount;
                }

                double buildMs = average(buildTimes);
                double acMs = average(acTimes);
                double kmpMs = average(kmpTimes);

                System.out.printf(
                        Locale.US,
                        "%-10d %-12d %-14.4f %-14.4f "
                                + "%-14.4f %-10d%n",
                        patternCount, textLength,
                        buildMs, acMs, kmpMs, matchCount
                );

                csv.append(String.format(
                        Locale.US,
                        "%d,%d,%.6f,%.6f,%.6f,%d%n",
                        patternCount, textLength,
                        buildMs, acMs, kmpMs, matchCount
                ));
            }
        }

        Files.writeString(
                Path.of("results.csv"),
                csv.toString()
        );

        System.out.println(
                "\nResults saved to results.csv");
    }
}
