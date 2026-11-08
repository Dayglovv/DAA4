package experiments;
import dp.EditDistance;
import dp.LCS;
import matching.KMP;
import matching.MatchResult;
import matching.NaiveSearch;
import matching.RabinKarp;
import suffix.Kasai;
import suffix.SuffixArray;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
public class Experiments {
    static final long SEED = 42;
    static final String ALPHABET = "ACGT";
    static final int RUNS = 5;
    static final int[] DP_SIZES = {100, 500, 1000};
    static final int[] TEXT_SIZES = {10_000, 50_000, 100_000};
    static final int[] KASAI_SIZES = {1000, 5000, 10_000};
    static final int PATTERN_LENGTH = 20;
    static final int WORST_PATTERN_LENGTH = 100;
    private interface Task {
        long[] run();
    }

    private static class Row {
        String experiment;
        String algorithm;
        String workload;
        int n;
        int m;
        long[] times = new long[RUNS];
        double avg;
        long[] counters;
    }

    private static long sink = 0;

    public static void run(String outputDir) throws IOException {
        collect();
        List<Row> rows = collect();
        writeResults(rows, outputDir);
        printTable(rows);
        System.out.println("(sink = " + sink + ")");
    }

    private static List<Row> collect() {
        List<Row> rows = new ArrayList<>();
        for (int size : DP_SIZES) {
            final String a = StringGenerator.random(size, ALPHABET, SEED);
            final String b = StringGenerator.random(size, ALPHABET, SEED + 1);
            rows.add(measure("DP", "LCS", "random strings", size, size, () -> {
                LCS.Result r = LCS.compute(a, b);
                sink += r.length;
                return new long[]{r.operations, -1, -1, -1};
            }));
            rows.add(measure("DP", "Edit Distance", "random strings", size, size, () -> {
                EditDistance.Result r = EditDistance.compute(a, b);
                sink += r.distance;
                return new long[]{r.operations, -1, -1, -1};
            }));
        }
        for (int size : TEXT_SIZES) {
            String text = StringGenerator.random(size, ALPHABET, SEED);
            String present = text.substring(size / 2, size / 2 + PATTERN_LENGTH);
            String absent = StringGenerator.randomAbsentPattern(text, PATTERN_LENGTH, ALPHABET, SEED + 99);
            addMatching(rows, "pattern present", text, present);
            addMatching(rows, "pattern absent", text, absent);

            String allA = StringGenerator.repeat('A', size);
            String nearMiss = StringGenerator.repeat('A', WORST_PATTERN_LENGTH - 1) + "C";
            String allMatch = StringGenerator.repeat('A', WORST_PATTERN_LENGTH);
            addMatching(rows, "worst: near miss (A^99 C)", allA, nearMiss);
            addMatching(rows, "worst: all match (A^100)", allA, allMatch);
        }

        for (int size : KASAI_SIZES) {
            final String s = StringGenerator.random(size, ALPHABET, SEED);
            final int[] sa = SuffixArray.buildSimple(s); // not timed
            rows.add(measure("Kasai", "Kasai (LCP)", "random string", size, size, () -> {
                Kasai.Result r = Kasai.build(s, sa);
                sink += r.lcp[size / 2];
                return new long[]{r.comparisons, -1, -1, -1};
            }));
        }
        return rows;
    }

    private static void addMatching(List<Row> rows, String workload, String text, String pattern) {
        final int n = text.length();
        final int m = pattern.length();
        rows.add(measure("Matching", "Naive", workload, n, m, () -> {
            MatchResult r = NaiveSearch.search(text, pattern);
            sink += r.indices.size();
            return new long[]{r.comparisons, r.preprocessingOps, r.indices.size(), -1};
        }));
        rows.add(measure("Matching", "KMP", workload, n, m, () -> {
            MatchResult r = KMP.search(text, pattern);
            sink += r.indices.size();
            return new long[]{r.comparisons, r.preprocessingOps, r.indices.size(), -1};
        }));
        rows.add(measure("Matching", "Rabin-Karp", workload, n, m, () -> {
            MatchResult r = RabinKarp.search(text, pattern);
            sink += r.indices.size();
            return new long[]{r.comparisons, r.preprocessingOps, r.indices.size(), r.falsePositives};
        }));
    }

    private static Row measure(String experiment, String algorithm, String workload, int n, int m, Task task) {
        long warmStart = System.nanoTime();
        int it = 0;
        while (it < 5 || (System.nanoTime() - warmStart < 100_000_000L && it < 20_000)) {
            task.run();
            it++;
        }
        System.gc();
        Row row = new Row();
        row.experiment = experiment;
        row.algorithm = algorithm;
        row.workload = workload;
        row.n = n;
        row.m = m;

        long sum = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            long[] counters = task.run();
            long end = System.nanoTime();
            row.times[i] = end - start;
            row.counters = counters;
            sum += row.times[i];
        }
        row.avg = sum / (double) RUNS;
        return row;
    }

    private static String fmt(long value) {
        return value < 0 ? "-" : String.valueOf(value);
    }

    private static void writeResults(List<Row> rows, String outputDir) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);

        try (PrintWriter out = new PrintWriter(dir.resolve("results.csv").toFile())) {
            out.println("experiment,algorithm,workload,n,m,run1_ns,run2_ns,run3_ns,run4_ns,run5_ns,avg_ns,"
                    + "operations,preprocessing_ops,matches,false_positives");
            for (Row r : rows) {
                out.print(r.experiment + "," + r.algorithm + "," + r.workload + "," + r.n + "," + r.m);
                for (long t : r.times) {
                    out.print("," + t);
                }
                out.println("," + String.format(Locale.US, "%.1f", r.avg) + "," + fmt(r.counters[0]) + ","
                        + fmt(r.counters[1]) + "," + fmt(r.counters[2]) + "," + fmt(r.counters[3]));
            }
        }
        try (PrintWriter out = new PrintWriter(dir.resolve("results.md").toFile())) {
            out.println("### LCS and Edit Distance");
            out.println();
            out.println("| Algorithm | Length n = m | Avg time (ns) | Table cells computed |");
            out.println("|---|---:|---:|---:|");
            for (Row r : rows) {
                if (r.experiment.equals("DP")) {
                    out.println("| " + r.algorithm + " | " + r.n + " | " + String.format(Locale.US, "%,.0f", r.avg)
                            + " | " + r.counters[0] + " |");
                }
            }
            out.println();
            out.println("### String matching");
            out.println();
            out.println("| Workload | Text length n | Pattern length m | Algorithm | Avg time (ns) | Char comparisons "
                    + "| Preprocessing ops | Matches | Hash collisions |");
            out.println("|---|---:|---:|---|---:|---:|---:|---:|---:|");
            for (Row r : rows) {
                if (r.experiment.equals("Matching")) {
                    out.println("| " + r.workload + " | " + r.n + " | " + r.m + " | " + r.algorithm + " | "
                            + String.format(Locale.US, "%,.0f", r.avg) + " | " + r.counters[0] + " | "
                            + fmt(r.counters[1]) + " | " + fmt(r.counters[2]) + " | " + fmt(r.counters[3]) + " |");
                }
            }
            out.println();
            out.println("### Kasai's algorithm");
            out.println();
            out.println("| String length n | Avg time (ns) | Char comparisons |");
            out.println("|---:|---:|---:|");
            for (Row r : rows) {
                if (r.experiment.equals("Kasai")) {
                    out.println("| " + r.n + " | " + String.format(Locale.US, "%,.0f", r.avg) + " | " + r.counters[0] + " |");
                }
            }
        }
    }

    private static void printTable(List<Row> rows) {
        System.out.printf("%-9s %-14s %-27s %8s %5s %16s %14s%n", "Exp", "Algorithm", "Workload", "n", "m", "Avg time (ns)", "Operations");
        for (Row r : rows) {
            System.out.printf("%-9s %-14s %-27s %8d %5d %16.0f %14d%n", r.experiment, r.algorithm, r.workload,
                    r.n, r.m, r.avg, r.counters[0]);
        }
    }
}
