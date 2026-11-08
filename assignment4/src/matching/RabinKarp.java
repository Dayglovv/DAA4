package matching;
import java.util.ArrayList;
import java.util.List;
public class RabinKarp {
    public static final long BASE = 256;
    public static final long DEFAULT_MOD = 1_000_000_007L; // a big prime number

    public static MatchResult search(String text, String pattern) {
        return search(text, pattern, DEFAULT_MOD);
    }
    public static MatchResult search(String text, String pattern, long mod) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return new MatchResult(result, 0, 0, 0, 0);
        }
        long h = 1;
        for (int k = 0; k < m - 1; k++) {
            h = (h * BASE) % mod;
        }
        long patternHash = 0;
        long windowHash = 0;
        for (int k = 0; k < m; k++) {
            patternHash = (patternHash * BASE + pattern.charAt(k)) % mod;
            windowHash = (windowHash * BASE + text.charAt(k)) % mod;
        }
        long preprocessingOps = 2L * m;

        long comparisons = 0;
        long hashUpdates = 0;
        long falsePositives = 0;
        for (int i = 0; i <= n - m; i++) {
            if (windowHash == patternHash) {
                int j = 0;
                while (j < m) {
                    comparisons++;
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        break;
                    }
                    j++;
                }
                if (j == m) {
                    result.add(i);
                } else {
                    falsePositives++;
                }
            }
            if (i < n - m) {
                windowHash = (windowHash - (text.charAt(i) * h) % mod + mod) % mod;
                windowHash = (windowHash * BASE + text.charAt(i + m)) % mod;
                hashUpdates++;
            }
        }
        return new MatchResult(result, comparisons, preprocessingOps, hashUpdates, falsePositives);
    }
}
