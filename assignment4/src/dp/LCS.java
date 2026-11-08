package dp;
public class LCS {

    public static class Result {
        public final int length;
        public final long operations;
        private final int[][] table;

        Result(int length, long operations, int[][] table) {
            this.length = length;
            this.operations = operations;
            this.table = table;
        }
        public String subsequence(String a, String b) {
            StringBuilder sb = new StringBuilder();
            int i = a.length();
            int j = b.length();
            while (i > 0 && j > 0) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    sb.append(a.charAt(i - 1));
                    i--;
                    j--;
                } else if (table[i - 1][j] >= table[i][j - 1]) {
                    i--;
                } else {
                    j--;
                }
            }
            return sb.reverse().toString();
        }
    }

    public static Result compute(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        long operations = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                operations++;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return new Result(dp[n][m], operations, dp);
    }
}
