package matching;
import java.util.ArrayList;
import java.util.List;
public class KMP {
    public static int[] buildLps(String pattern, long[] counter) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0;
        int i = 1;
        while (i < m) {
            counter[0]++;
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public static MatchResult search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new MatchResult(result, 0, 0, 0, 0);
        }
        long[] prepCounter = {0};
        int[] lps = buildLps(pattern, prepCounter);

        long comparisons = 0;
        int i = 0;
        int j = 0;
        while (i < n) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    result.add(i - m);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return new MatchResult(result, comparisons, prepCounter[0], 0, 0);
    }
}
