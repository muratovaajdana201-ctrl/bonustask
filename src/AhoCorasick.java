
import java.util.*;

public class AhoCorasick {

    private static class Node {
        Map<Character, Node> next = new HashMap<>();
        Node fail;
        Node outputLink;
        List<Integer> patternIds = new ArrayList<>();
    }

    public static class Match {
        public final int patternId;
        public final int start;
        public final int end;

        public Match(int patternId, int start, int end) {
            this.patternId = patternId;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Pattern " + patternId
                    + " at [" + start + ", " + end + "]";
        }
    }

    private final Node root = new Node();
    private final List<String> patterns = new ArrayList<>();
    private boolean built = false;

    public void addPattern(String pattern) {
        if (built) {
            throw new IllegalStateException(
                    "Cannot add patterns after build()");
        }

        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException(
                    "Pattern must not be empty");
        }

        int id = patterns.size();
        patterns.add(pattern);

        Node current = root;

        for (char c : pattern.toCharArray()) {
            current = current.next.computeIfAbsent(
                    c, key -> new Node());
        }

        current.patternIds.add(id);
    }

    public void build() {
        if (built) {
            return;
        }

        Queue<Node> queue = new ArrayDeque<>();
        root.fail = root;

        for (Node child : root.next.values()) {
            child.fail = root;
            child.outputLink = null;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry
                    : current.next.entrySet()) {

                char c = entry.getKey();
                Node child = entry.getValue();
                Node fallback = current.fail;

                while (fallback != root
                        && !fallback.next.containsKey(c)) {
                    fallback = fallback.fail;
                }

                Node candidate = fallback.next.get(c);

                if (candidate != null) {
                    child.fail = candidate;
                } else {
                    child.fail = root;
                }

                if (!child.fail.patternIds.isEmpty()) {
                    child.outputLink = child.fail;
                } else {
                    child.outputLink = child.fail.outputLink;
                }

                queue.add(child);
            }
        }

        built = true;
    }

    public List<Match> search(String text) {
        if (!built) {
            throw new IllegalStateException(
                    "Call build() before search()");
        }

        List<Match> matches = new ArrayList<>();
        Node current = root;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (current != root
                    && !current.next.containsKey(c)) {
                current = current.fail;
            }

            Node nextNode = current.next.get(c);
            current = nextNode == null ? root : nextNode;

            Node output = current;

            while (output != null) {
                for (int id : output.patternIds) {
                    int start = i - patterns.get(id).length() + 1;
                    matches.add(new Match(id, start, i));
                }

                output = output.outputLink;
            }
        }

        return matches;
    }

    public long countMatches(String text) {
        if (!built) {
            throw new IllegalStateException(
                    "Call build() before search()");
        }

        long count = 0;
        Node current = root;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            while (current != root
                    && !current.next.containsKey(c)) {
                current = current.fail;
            }

            Node nextNode = current.next.get(c);
            current = nextNode == null ? root : nextNode;

            Node output = current;

            while (output != null) {
                count += output.patternIds.size();
                output = output.outputLink;
            }
        }

        return count;
    }

    public String getPattern(int id) {
        return patterns.get(id);
    }

    public int getPatternCount() {
        return patterns.size();
    }
}
