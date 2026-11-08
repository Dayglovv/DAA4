import dp.EditDistance;
import dp.LCS;
import experiments.Experiments;
import matching.KMP;
import matching.MatchResult;
import matching.NaiveSearch;
import matching.RabinKarp;
import suffix.Kasai;
import suffix.SuffixArray;
import java.io.IOException;
import java.util.Arrays;
public class Main {
    public static void main(String[] args) throws IOException {
        String mode = args.length > 0 ? args[0] : "all";
        if (mode.equals("demo") || mode.equals("all")) {
            demo();
        }
        if (mode.equals("experiments") || mode.equals("all")) {
            System.out.println("\n=== Experiments (alphabet ACGT, seed 42, 5 runs each) ===");
            Experiments.run("results/tables");
            System.out.println("Tables saved to results/tables/");
        }
    }

    static void demo() {
        System.out.println("=== 1. LCS ===");
        String a = "AGGTAB";
        String b = "GXTXAYB";
        LCS.Result lcs = LCS.compute(a, b);
        System.out.println("LCS(" + a + ", " + b + ") = " + lcs.length + "  subsequence: " + lcs.subsequence(a, b));

        System.out.println("\n=== 2. Edit distance ===");
        EditDistance.Result ed = EditDistance.compute("kitten", "sitting");
        System.out.println("distance(kitten, sitting) = " + ed.distance);
        System.out.print(ed.script("kitten", "sitting"));

        System.out.println("\n=== 3. String matching ===");
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";
        System.out.println("text:    " + text);
        System.out.println("pattern: " + pattern);
        MatchResult naive = NaiveSearch.search(text, pattern);
        MatchResult kmp = KMP.search(text, pattern);
        MatchResult rk = RabinKarp.search(text, pattern);
        System.out.println("Naive:        " + naive.indices + "  comparisons: " + naive.comparisons);
        System.out.println("KMP:          " + kmp.indices + "  comparisons: " + kmp.comparisons
                + " (+ " + kmp.preprocessingOps + " for the lps table)");
        System.out.println("Rabin-Karp:   " + rk.indices + "  comparisons: " + rk.comparisons
                + ", false positives: " + rk.falsePositives);

        System.out.println("\n=== 4. Suffix array and LCP array (Kasai) ===");
        String s = "banana";
        int[] sa = SuffixArray.buildSimple(s);
        int[] lcp = Kasai.build(s, sa).lcp;
        System.out.println("string: " + s);
        System.out.println("i   sa[i]  lcp[i]  suffix");
        for (int i = 0; i < s.length(); i++) {
            System.out.printf("%d   %d      %d       %s%n", i, sa[i], lcp[i], s.substring(sa[i]));
        }
        System.out.println("SA  = " + Arrays.toString(sa));
        System.out.println("LCP = " + Arrays.toString(lcp));
    }
}
