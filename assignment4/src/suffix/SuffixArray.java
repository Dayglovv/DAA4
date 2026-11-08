package suffix;
import java.util.Arrays;
public class SuffixArray {
    public static int[] buildSimple(String s) {
        int n = s.length();
        Integer[] boxed = new Integer[n];
        for (int i = 0; i < n; i++) {
            boxed[i] = i;
        }
        Arrays.sort(boxed, (x, y) -> compareSuffixes(s, x, y));
        int[] sa = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = boxed[i];
        }
        return sa;
    }
    private static int compareSuffixes(String s, int a, int b) {
        int n = s.length();
        while (a < n && b < n) {
            char ca = s.charAt(a);
            char cb = s.charAt(b);
            if (ca != cb) {
                return Character.compare(ca, cb);
            }
            a++;
            b++;
        }
        return Integer.compare(n - a, n - b);
    }
}
