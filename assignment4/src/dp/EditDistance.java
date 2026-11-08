package dp;
public class EditDistance {

    public static class Result {
        public final int distance;
        public final long operations;
        private final int[][] table;

        Result(int distance, long operations, int[][] table) {
            this.distance = distance;
            this.operations = operations;
            this.table = table;
        }
        public String script(String a, String b) {
            StringBuilder sb = new StringBuilder();
            int i = a.length();
            int j = b.length();
            while (i > 0 || j > 0) {
                if (i > 0 && j > 0 && a.charAt(i - 1) == b.charAt(j - 1)
                        && table[i][j] == table[i - 1][j - 1]) {
                    i--;
                    j--;
                } else if (i > 0 && j > 0 && table[i][j] == table[i - 1][j - 1] + 1) {
                    sb.append("replace '").append(a.charAt(i - 1)).append("' with '").append(b.charAt(j - 1)).append("'\n");
                    i--;
                    j--;
                } else if (i > 0 && table[i][j] == table[i - 1][j] + 1) {
                    sb.append("delete '").append(a.charAt(i - 1)).append("'\n");
                    i--;
                } else {
                    sb.append("insert '").append(b.charAt(j - 1)).append("'\n");
                    j--;
                }
            }
            return sb.toString();
        }
    }

    public static Result compute(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        long operations = 0;

        for (int i = 0; i <= n; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                operations++;
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                int delete = dp[i - 1][j] + 1;
                int insert = dp[i][j - 1] + 1;
                int replace = dp[i - 1][j - 1] + cost;
                dp[i][j] = Math.min(replace, Math.min(delete, insert));
            }
        }
        return new Result(dp[n][m], operations, dp);
    }
}
