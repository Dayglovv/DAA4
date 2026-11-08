package suffix;
public class Kasai {
    public static class Result {
        public final int[] lcp;
        public final long comparisons;

        Result(int[] lcp, long comparisons) {
            this.lcp = lcp;
            this.comparisons = comparisons;
        }
    }

    public static Result build(String s, int[] sa) {
        int n = s.length();
        if (sa.length != n) {
            throw new IllegalArgumentException("Suffix array must have the same length as the string");
        }
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            rank[sa[i]] = i;
        }

        int[] lcp = new int[n];
        long comparisons = 0;
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n) {
                    comparisons++;
                    if (s.charAt(i + h) != s.charAt(j + h)) {
                        break;
                    }
                    h++;
                }
                lcp[rank[i]] = h;
                if (h > 0) {
                    h--;
                }
            } else {
                h = 0;
            }
        }
        return new Result(lcp, comparisons);
    }
}
