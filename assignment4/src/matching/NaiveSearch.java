package matching;
import java.util.ArrayList;
import java.util.List;
public class NaiveSearch {
    public static MatchResult search(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        long comparisons = 0;

        if (m == 0 || m > n) {
            return new MatchResult(result, 0, 0, 0, 0);
        }

        for (int shift = 0; shift <= n - m; shift++) {
            int j = 0;
            while (j < m) {
                comparisons++;
                if (text.charAt(shift + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == m) {
                result.add(shift);
            }
        }
        return new MatchResult(result, comparisons, 0, 0, 0);
    }
}
