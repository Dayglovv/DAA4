# Assignment 4 — Text Analysis: Dynamic Programming and String Algorithms

Java application with algorithms for text analysis:

- **Part A (dynamic programming):** Longest Common Subsequence (LCS) and Edit Distance (Levenshtein).
- **Part B (string matching):** Naive search, Knuth–Morris–Pratt (KMP) and Rabin–Karp.
- **Part C (advanced):** Kasai's algorithm for the LCP array (with a simple suffix array builder for the tests).

All algorithms are implemented by myself (no library methods like `String.indexOf` or `String.contains` are used in the
algorithms). Java standard library is used only for data structures, random numbers, sorting inside the simple suffix array
builder and timing.

## Contents

1. [Problem description and algorithm overview](#1-problem-description-and-algorithm-overview)
2. [Implementation decisions and assumptions](#2-implementation-decisions-and-assumptions)
3. [Complexity analysis](#3-complexity-analysis)
4. [Testing strategy and results](#4-testing-strategy-and-results)
5. [Experimental setup](#5-experimental-setup)
6. [Results: tables and plots](#6-results-tables-and-plots)
7. [Discussion: theory vs. experiments](#7-discussion-theory-vs-experiments)
8. [Conclusion](#8-conclusion)

## How to build and run

```
# from the project root (assignment-4/)
javac -d out $(find src -name "*.java")

java -cp out Main                 # demo + all experiments (tables are saved to results/tables)
java -cp out Main demo            # only the demo
java -cp out Main experiments     # only the experiments
java -cp out tests.Tests          # all tests (prints PASS/FAIL, exit code 1 if something fails)

python3 results/plots/make_plots.py   # builds the plots from results/tables/results.csv (needs matplotlib)
```

In IntelliJ IDEA: mark `src` as *Sources Root*, then run `Main` or `tests.Tests`. No external libraries are needed for Java.
Python with matplotlib is used **only** for drawing the plots; the experiments themselves are in Java.

### Project structure

```
assignment-4/
├── src/
│   ├── Main.java                  demo + running the experiments
│   ├── dp/                        LCS, EditDistance
│   ├── matching/                  NaiveSearch, KMP, RabinKarp, MatchResult
│   ├── suffix/                    SuffixArray (simple builder), Kasai
│   ├── experiments/               Experiments, StringGenerator
│   └── tests/                     Tests
├── results/
│   ├── tables/                    results.csv, results.md, test_output.txt
│   └── plots/                     4 plots + make_plots.py
└── README.md
```

## 1. Problem description and algorithm overview

| Part | Algorithm | What it does |
|---|---|---|
| A | **LCS** | Length of the longest subsequence (characters in the same order, but not necessarily next to each other) that two strings share. Example: LCS("AGGTAB", "GXTXAYB") = 4 ("GTAB"). |
| A | **Edit Distance** | Minimum number of insertions, deletions and substitutions to turn one string into another. Example: kitten → sitting = 3. |
| B | **Naive matching** | Tries the pattern at every position of the text and compares character by character. Baseline. |
| B | **KMP** | Builds the *LPS table* of the pattern first, and uses it to never go back in the text after a mismatch. |
| B | **Rabin–Karp** | Compares *hash values* of the pattern and of every window of the text. The hash of the next window is computed in O(1) (rolling hash). If hashes are equal, the real characters are checked. |
| C | **Kasai** | Builds the LCP array (longest common prefix of neighbouring suffixes in sorted order) in O(n) from a string and its suffix array. |

### Part A: why the recurrences are correct

**LCS.** `dp[i][j]` = LCS length of the first `i` characters of `a` and the first `j` characters of `b`.

- Base cases: `dp[0][j] = dp[i][0] = 0`, because the empty string has no common characters with anything.
- If `a[i-1] == b[j-1]`: these two last characters can be matched with each other. Some optimal solution always does this
  (if an optimal solution did not use one of them, we could replace its last matched pair by this pair without making it
  shorter), so `dp[i][j] = dp[i-1][j-1] + 1`.
- If they are different: they cannot both be the last matched pair, so at least one of them is not used. Then the answer is the
  better of dropping the last character of `a` or the last character of `b`: `dp[i][j] = max(dp[i-1][j], dp[i][j-1])`.

Every cell depends only on cells with smaller `i` or `j`, so filling the table row by row is a correct order.

**Edit Distance.** `dp[i][j]` = minimal number of operations to turn `a[0..i)` into `b[0..j)`.

- Base cases: `dp[i][0] = i` (delete `i` characters) and `dp[0][j] = j` (insert `j` characters).
- Look at the last characters. The last step of any optimal solution is one of three things:
  delete `a[i-1]` (`dp[i-1][j] + 1`), insert `b[j-1]` (`dp[i][j-1] + 1`), or make `a[i-1]` and `b[j-1]` equal
  (`dp[i-1][j-1] + 0` if they are already equal, or `+ 1` for a substitution). We take the minimum of the three, and because
  each smaller problem is solved optimally, the result is optimal.

### Part B: how KMP avoids repeated comparisons and how Rabin–Karp handles collisions

**KMP.** `lps[i]` is the length of the longest *proper* prefix of `pattern[0..i]` that is also a suffix of it.
Suppose `j` characters of the pattern matched the text and then a mismatch happened. The last `lps[j-1]` of these `j`
characters are also the beginning of the pattern, so we already know that they match the text. We keep the text pointer
`i` where it is and only set `j = lps[j-1]`. The pointer `i` never moves back, and `j` can only decrease as many times
as it was increased before, so the whole search makes at most about `2n` comparisons, whatever the pattern is. The table itself
is built with the same idea in O(m).

**Rabin–Karp.** The rolling hash gives equal values for equal strings, but different strings can also get equal hash values
(*collision*, also called spurious hit). That is why equal hashes are only a *candidate*: the algorithm then compares the real
characters one by one. If they are different, the collision is counted as a *false positive* and the search just continues.
So the result is always correct, and the hash only affects the speed. I tested this with a very small modulus (7), which
produces a lot of collisions (see section 4).

### Part C: what the LCP array represents

For a string `s` the suffix array `sa` contains the starting indices of all suffixes in alphabetical order.
`lcp[i]` is the length of the longest common prefix of the suffixes `sa[i-1]` and `sa[i]` (and `lcp[0] = 0` by convention).

Example for `banana`:

| i | sa[i] | lcp[i] | suffix |
|---:|---:|---:|---|
| 0 | 5 | 0 | a |
| 1 | 3 | 1 | ana |
| 2 | 1 | 3 | anana |
| 3 | 0 | 0 | banana |
| 4 | 4 | 0 | na |
| 5 | 2 | 2 | nana |

The LCP array tells how much neighbouring suffixes have in common. For example, the maximum value of `lcp` is the length of the longest
substring that occurs at least twice in the string (here `ana`, 3), and the number of different substrings can be computed from it.

**Kasai's idea.** Process the suffixes in *text order* (`i = 0, 1, 2, ...`), not in sorted order. If suffix `i` has common prefix
`h` with the suffix just before it in the sorted order, then suffix `i+1` has a common prefix of at least `h-1` with *its* predecessor
(we delete the first character of both suffixes). So instead of starting from zero we continue from `h-1`.
`h` is decreased at most once per step (so at most `n` times in total) and it is never bigger than `n`, so it can be increased at most `2n` times
in total. Together with at most `n` comparisons that stop the loop, all comparisons are O(n) (at most `3n`).

## 2. Implementation decisions and assumptions

- **Strings and characters.** Strings are Java `String`s, characters are compared by their `char` code, so the comparison is
  **case-sensitive**. Indices are **0-based**.
- **Empty pattern.** An empty pattern, and a pattern that is longer than the text, give an **empty list** for all three matching
  algorithms (there is nothing meaningful to search). This is an assumption I chose; another possible convention is to return all
  positions `0..n`. An empty text always gives an empty list.
- **Overlapping occurrences** are all reported (`"AA"` in `"AAAAA"` gives `[0, 1, 2, 3]`). After a full match KMP continues with
  `j = lps[m-1]`, Naive and Rabin–Karp simply go to the next shift.
- **LCS and Edit Distance** use the full `(n+1) x (m+1)` table of `int`. This is needed to restore the answer (`subsequence(...)` for LCS
  and `script(...)` for the list of edit operations). If only the length is needed, two rows are enough and the space is O(min(n, m)),
  but I did not use this version in the experiments. All edit operations cost 1.
- **Rabin–Karp** uses base 256 and the prime modulus 1 000 000 007 (all arithmetic fits in `long`). The modulus can be passed as a parameter
  (`RabinKarp.search(text, pattern, mod)`), which I use in the tests to force collisions.
- **Kasai.** The input is the string and its suffix array; `lcp[0] = 0`. The method checks that `sa.length == s.length()`.
  For the tests and experiments the suffix array is built by `SuffixArray.buildSimple`: it just sorts the suffixes with a comparator that
  compares characters directly. It is O(n² log n) in the worst case (for example `"aaaa..."`), but fast for random strings. As allowed in the
  assignment, I did not implement a fast construction algorithm. Suffix array building is **never inside the timed region**.
- **Operation counts.**
  LCS and Edit Distance: number of table cells computed (= n·m, every cell needs one character comparison).
  Naive: character comparisons. KMP: character comparisons in the search, and the comparisons for building the LPS table are given separately
  (*preprocessing ops*). Rabin–Karp: character comparisons made only during *verification*; the rolling hash updates and the
  hash collisions (false positives) are counted separately. Kasai: character comparisons in the `while` loop.
  The counters are plain `long` variables and do not change the complexity.

## 3. Complexity analysis

`n` = text length (or length of the first string), `m` = pattern length (or length of the second string).
This table shows the **theoretical** complexity. Measured times are in sections 6 and 7.

| Algorithm | Main operation / recurrence | Best case | Worst case | Auxiliary space | Why |
|---|---|---|---|---|---|
| **LCS** | `dp[i][j] = dp[i-1][j-1] + 1` if chars equal, else `max(dp[i-1][j], dp[i][j-1])` | Θ(n·m) | Θ(n·m) | O(n·m) (O(min(n,m)) if only the length is needed) | The table has (n+1)(m+1) cells and every cell is computed in O(1). The work does not depend on the content of the strings. |
| **Edit Distance** | `dp[i][j] = min(dp[i-1][j]+1, dp[i][j-1]+1, dp[i-1][j-1]+cost)` | Θ(n·m) | Θ(n·m) | O(n·m) (O(min(n,m)) for the distance only) | Same table size, O(1) per cell (three candidates). |
| **Naive search** | compare the pattern with the text at every shift | O(n) (first character differs at every shift) | O((n−m+1)·m) = O(n·m) | O(1) | n−m+1 shifts, each needs at most m comparisons. The worst case is when almost the whole pattern matches at every shift (`AAAA…A` with pattern `AA…AC`). |
| **KMP** | `lps` table + `j = lps[j-1]` on a mismatch | O(n + m) | O(n + m) | O(m) | Preprocessing O(m). In the search the text pointer never goes back, and `j` can not decrease more times than it increased, so ≤ 2n comparisons. |
| **Rabin–Karp** | rolling hash `h' = ((h − s[i]·B^(m−1))·B + s[i+m]) mod Q`, verify when hashes are equal | O(n + m) | O(n·m) | O(1) | Preprocessing O(m) (two hashes), each window update is O(1). Expected O(n + m) because for a good hash the chance of a collision per window is about 1/Q. Worst case: many windows have to be verified (every window really matches, as in `AAAA…A` with `AA…A`, or a bad hash with many collisions) and each verification costs O(m). |
| **Kasai (LCP)** | `h` = current LCP, decreases by at most 1 per step | O(n) | O(n) | O(n) (rank array) + O(n) for the output | Two loops over n suffixes; `h` decreases at most n times and never exceeds n, so it increases at most 2n times in total; the `while` loop makes at most 3n comparisons, O(n). |

**Expected vs. worst case.** KMP and Kasai have the same bound in every case, so there is no difference. Naive and Rabin–Karp differ:
the *expected* time for random text is linear, but the *worst case* is O(n·m). For Naive, the expected number of comparisons
per shift on a random text depends on the alphabet size: with `σ` letters it is about `1 / (1 − 1/σ)`, which is 1.33 for my
4-letter alphabet. For Rabin–Karp the expected time is linear because the probability of a false positive per window is small
when the modulus is large; the worst case O(n·m) can also appear without any collisions, only because the pattern really matches everywhere.

Theoretical complexity says how the number of steps *grows*. It ignores constants, the processor cache, the JVM and so on.
That is why in section 7 I compare the *trends* of measured times with the theory, not the exact numbers.

## 4. Testing strategy and results

Tests are in `src/tests/Tests.java` (no JUnit, a small `check(name, condition)` method prints PASS/FAIL).
Run: `java -cp out tests.Tests`. The output of my run is saved in `results/tables/test_output.txt`:
**61 checks, all passed.**

| Algorithm | Cases from the assignment | Examples of checks |
|---|---|---|
| LCS | identical strings, completely different strings, empty string | `LCS(ALGORITHM, ALGORITHM) = 9`; `LCS(AAAA, BBBB) = 0`; empty first / second / both strings → 0; `LCS(ABCBDAB, BDCABA) = 4`; restored subsequence of `AGGTAB`/`GXTXAYB` is `GTAB` and is really a subsequence of both strings |
| Edit Distance | identical, one empty, substitution, insertion, deletion | `kitten/kitten = 0`; `"" → abcd = 4`; `cat → cut = 1` (substitution); `cat → cart = 1` (insertion); `cart → cat = 1` (deletion); `kitten → sitting = 3`; `flaw → lawn = 2`; `intention → execution = 5`; the restored script has exactly `distance` operations |
| String matching | pattern at the beginning, in the middle, at the end; multiple occurrences; absent; longer than the text; empty pattern | each case is checked for **all three algorithms at once**: Naive, KMP and Rabin–Karp must all return exactly the expected list of indices. Extra cases: overlapping occurrences (`AA` in `AAAAA` → `[0,1,2,3]`), pattern equal to the text, one-character pattern, empty text |
| KMP internals | LPS table | `lps(AABAACAABAA) = [0,1,0,1,2,0,1,2,3,4,5]` (checked by hand), `lps(AAAA) = [0,1,2,3]`, `lps(ABCDE) = [0,0,0,0,0]` |
| Rabin–Karp collisions | hash collisions | with modulus 7 the result is still correct and the algorithm really reported false positives; with the big prime there were none on that input |
| Kasai | short string with a manually verified SA and LCP | `banana`: SA = `[5,3,1,0,4,2]`, LCP = `[0,1,3,0,0,2]`; `mississippi`: SA = `[10,7,4,1,0,9,8,6,3,5,2]`, LCP = `[0,1,1,4,0,0,1,0,2,1,3]` (both checked by hand); `aaaa`, a single character, the empty string; a wrong suffix array length throws an exception; the number of comparisons is ≤ 3n |
| Random cross-checks | agreement between algorithms and properties | 300 small random cases: Naive == KMP == Rabin–Karp (also with modulus 5) == a simple reference; 20 texts of length 5000 with many matches; 200 random pairs for LCS / Edit Distance properties (both symmetric, LCS ≤ min length, edit distance between the length difference and the longer length, edit distance ≤ n + m − 2·LCS); 100 random strings: Kasai == brute-force LCP and the suffix array is really sorted |

**Manual check of the Kasai test (banana).** Sorted suffixes: `a`, `ana`, `anana`, `banana`, `na`, `nana`.
Neighbouring pairs: `a`/`ana` share `a` (1); `ana`/`anana` share `ana` (3); `anana`/`banana` share nothing (0);
`banana`/`na` (0); `na`/`nana` share `na` (2). With `lcp[0] = 0`: `[0, 1, 3, 0, 0, 2]`, which is what the program returns.

## 5. Experimental setup

| Experiment | Input |
|---|---|
| LCS | two random strings of length 100, 500, 1000 |
| Edit Distance | the same two strings as for LCS (same inputs for both algorithms) |
| String matching | random text of length 10 000, 50 000, 100 000; pattern length 20; pattern **present** (a substring of the text, taken from the middle) and pattern **absent** (a random pattern, checked with my own naive search that it does not occur) |
| String matching, worst cases (extra) | text `AAA…A`; pattern `A^99 C` (*near miss*: almost matches at every position, never fully) and pattern `A^100` (*all match*: occurs at every position); pattern length 100 |
| Kasai | random string of length 1 000, 5 000, 10 000; the suffix array is built before the timer starts |

- **Alphabet:** `{A, C, G, T}` (4 letters) for every random string, in every experiment.
- **Random seed:** fixed, `42` (the second string of the LCS / Edit Distance pair uses seed `43`, the absent pattern uses `141`),
  so every run gives exactly the same inputs.
- **Same inputs for the same task:** LCS and Edit Distance get the same string pair; Naive, KMP and Rabin–Karp get the same text and the same
  pattern in every comparison.
- **Timing:** `System.nanoTime()` just before and just after one call of the algorithm. Each timed task is run **5 times** and the
  **average** is reported (the times of all 5 runs are in `results/tables/results.csv`).
- **Not in the timed region:** input generation, building of the suffix array, console output, file writing. The memory allocation of the
  DP table is inside the timed region because it is part of the algorithm.
- **Warm-up:** Java code is slow in the beginning (interpreter, then JIT compilation). So the whole experiment is run once and thrown away,
  and before every measurement the task is also executed for about 100 ms (not measured). `System.gc()` is called before the measured runs.
- **Operation counts:** see the end of section 2.
- All results come from my own implementation and one session of the program on one computer, so absolute times will be different on other
  machines. Only the trends should be compared.

## 6. Results: tables and plots

Full tables are in `results/tables/results.md`, the times of all 5 runs for every row are in `results/tables/results.csv`.

### LCS and Edit Distance

| Algorithm | Length n = m | Avg time (ns) | Table cells computed |
|---|---:|---:|---:|
| LCS | 100 | 17,638 | 10000 |
| Edit Distance | 100 | 51,917 | 10000 |
| LCS | 500 | 455,775 | 250000 |
| Edit Distance | 500 | 827,472 | 250000 |
| LCS | 1000 | 1,544,116 | 1000000 |
| Edit Distance | 1000 | 3,051,854 | 1000000 |

### String matching

| Workload | Text length n | Pattern length m | Algorithm | Avg time (ns) | Char comparisons | Preprocessing ops | Matches | Hash collisions |
|---|---:|---:|---|---:|---:|---:|---:|---:|
| pattern present | 10000 | 20 | Naive | 82,291 | 13433 | 0 | 1 | - |
| pattern present | 10000 | 20 | KMP | 76,253 | 12521 | 23 | 1 | - |
| pattern present | 10000 | 20 | Rabin-Karp | 159,275 | 20 | 40 | 1 | 0 |
| pattern absent | 10000 | 20 | Naive | 107,548 | 13370 | 0 | 0 | - |
| pattern absent | 10000 | 20 | KMP | 81,422 | 12488 | 22 | 0 | - |
| pattern absent | 10000 | 20 | Rabin-Karp | 162,723 | 0 | 40 | 0 | 0 |
| worst: near miss (A^99 C) | 10000 | 100 | Naive | 1,340,785 | 990100 | 0 | 0 | - |
| worst: near miss (A^99 C) | 10000 | 100 | KMP | 59,356 | 19901 | 197 | 0 | - |
| worst: near miss (A^99 C) | 10000 | 100 | Rabin-Karp | 162,177 | 0 | 200 | 0 | 0 |
| worst: all match (A^100) | 10000 | 100 | Naive | 1,077,660 | 990100 | 0 | 9901 | - |
| worst: all match (A^100) | 10000 | 100 | KMP | 110,410 | 10000 | 99 | 9901 | - |
| worst: all match (A^100) | 10000 | 100 | Rabin-Karp | 958,451 | 990100 | 200 | 9901 | 0 |
| pattern present | 50000 | 20 | Naive | 382,271 | 66511 | 0 | 1 | - |
| pattern present | 50000 | 20 | KMP | 398,693 | 62389 | 22 | 1 | - |
| pattern present | 50000 | 20 | Rabin-Karp | 800,761 | 20 | 40 | 1 | 0 |
| pattern absent | 50000 | 20 | Naive | 412,375 | 66746 | 0 | 0 | - |
| pattern absent | 50000 | 20 | KMP | 394,768 | 62361 | 22 | 0 | - |
| pattern absent | 50000 | 20 | Rabin-Karp | 793,589 | 0 | 40 | 0 | 0 |
| worst: near miss (A^99 C) | 50000 | 100 | Naive | 6,115,115 | 4990100 | 0 | 0 | - |
| worst: near miss (A^99 C) | 50000 | 100 | KMP | 300,078 | 99901 | 197 | 0 | - |
| worst: near miss (A^99 C) | 50000 | 100 | Rabin-Karp | 797,257 | 0 | 200 | 0 | 0 |
| worst: all match (A^100) | 50000 | 100 | Naive | 7,151,096 | 4990100 | 0 | 49901 | - |
| worst: all match (A^100) | 50000 | 100 | KMP | 461,966 | 50000 | 99 | 49901 | - |
| worst: all match (A^100) | 50000 | 100 | Rabin-Karp | 3,110,070 | 4990100 | 200 | 49901 | 0 |
| pattern present | 100000 | 20 | Naive | 769,243 | 133385 | 0 | 1 | - |
| pattern present | 100000 | 20 | KMP | 865,033 | 124915 | 22 | 1 | - |
| pattern present | 100000 | 20 | Rabin-Karp | 1,632,631 | 20 | 40 | 1 | 0 |
| pattern absent | 100000 | 20 | Naive | 777,416 | 133155 | 0 | 0 | - |
| pattern absent | 100000 | 20 | KMP | 775,720 | 124466 | 22 | 0 | - |
| pattern absent | 100000 | 20 | Rabin-Karp | 1,578,573 | 0 | 40 | 0 | 0 |
| worst: near miss (A^99 C) | 100000 | 100 | Naive | 11,457,266 | 9990100 | 0 | 0 | - |
| worst: near miss (A^99 C) | 100000 | 100 | KMP | 1,111,909 | 199901 | 197 | 0 | - |
| worst: near miss (A^99 C) | 100000 | 100 | Rabin-Karp | 1,580,321 | 0 | 200 | 0 | 0 |
| worst: all match (A^100) | 100000 | 100 | Naive | 15,273,208 | 9990100 | 0 | 99901 | - |
| worst: all match (A^100) | 100000 | 100 | KMP | 894,738 | 100000 | 99 | 99901 | - |
| worst: all match (A^100) | 100000 | 100 | Rabin-Karp | 5,875,547 | 9990100 | 200 | 99901 | 0 |

### Kasai's algorithm

| String length n | Avg time (ns) | Char comparisons |
|---:|---:|---:|
| 1000 | 14,096 | 1990 |
| 5000 | 70,230 | 9987 |
| 10000 | 147,654 | 19989 |

### Plots

**1. LCS and Edit Distance: execution time vs. input size** (log–log scale, the dashed line grows like n²)

![LCS and Edit Distance](../../../OneDrive/OOP/DAA4/assignment4/results/plots/lcs_edit_time.png)

**2. String matching on random text: execution time vs. text length** (pattern present / pattern absent, m = 20, the dashed line grows like n)

![String matching](../../../OneDrive/OOP/DAA4/assignment4/results/plots/matching_time.png)

**3. Extra: worst-case workloads for string matching** (text `AAA…A`, m = 100)

![Worst cases](../../../OneDrive/OOP/DAA4/assignment4/results/plots/matching_worst_case.png)

**4. Extra: Kasai's algorithm** (the dashed line grows like n)

![Kasai](../../../OneDrive/OOP/DAA4/assignment4/results/plots/kasai_time.png)

## 7. Discussion: theory vs. experiments

**LCS and Edit Distance.** Both have Θ(n·m) time, so for two strings of length n the time should grow like n² (the table has exactly
n² cells: 10 000, 250 000 and 1 000 000). In the measurements, going from n = 100 to n = 1000 (10× longer) made LCS about 88× slower
(17.6 µs → 1.54 ms) and Edit Distance about 59× slower (51.9 µs → 3.05 ms), while the theory says 100×. The slope of the plot is close to the
n² reference line, so the trend agrees with the theory. The measured ratio is a bit below 100, probably because at n = 100 the run takes only
tens of microseconds and fixed costs (allocating the table, noise) are a large part of it; I did not test this separately. Edit Distance
was about 1.8–2× slower than LCS for n = 500 and n = 1000, although both compute the same number of cells. A likely reason is that Edit Distance
does more work per cell (three candidates and the cost of a substitution, and the initialisation of the first row and column), but again this is
my interpretation, not something I measured.

**String matching on random text.** On a random text over 4 letters Naive was practically as fast as KMP: the times are the same within the noise
(for example 769 µs vs. 865 µs for n = 100 000 with the pattern present, and 777 µs vs. 776 µs with the pattern absent). The operation counts explain it:
Naive made 1.33–1.34 character comparisons per text position (133 385 comparisons for n = 100 000), exactly what is expected for a 4-letter alphabet
(`1 / (1 − 1/4) = 1.33`), because a random pattern mismatches after one or two characters. KMP needed about 1.25 comparisons per position. So on random text with
a small pattern the expected case of Naive is linear and there is nothing to save for KMP. Present and absent patterns also give almost the same
times: Naive and KMP must read the whole text anyway (the present pattern occurred once in the middle), and the differences between them are inside the noise.

**Rabin–Karp was the slowest on random text**, about 2× slower than the other two (1.63 ms vs. 0.77 ms for n = 100 000), although it made almost no character
comparisons (20 for the present pattern, 0 for the absent one, and **0 hash collisions** in all experiments with the big prime modulus). The reason is that
every window update uses two `%` operations on `long` values, which are much more expensive than the 1.3 character comparisons that Naive does per position.
This shows that the number of operations of different types can not be compared directly: Rabin–Karp has the fewest character comparisons, but not the best time.
Its advantages (for example searching for many patterns at once) were not tested here.

**Worst cases (extra experiment).** The text `AAA…A` shows the difference between the theoretical bounds very clearly (n = 100 000, m = 100):

| Workload | Naive | KMP | Rabin–Karp |
|---|---|---|---|
| near miss (`A^99 C`) | 11.5 ms, 9 990 100 comparisons | 1.11 ms, 199 901 comparisons | 1.58 ms, 0 comparisons |
| all match (`A^100`) | 15.3 ms, 9 990 100 comparisons | 0.89 ms, 100 000 comparisons | 5.88 ms, 9 990 100 comparisons |

- Naive makes (n − m + 1)·m ≈ 10⁷ comparisons, which is the O(n·m) worst case. With m fixed its time grows roughly linearly in n,
  but its constant is large: it is 10–17× slower than KMP for n = 100 000.
- KMP stays linear: 199 901 comparisons ≈ 2n for the near miss (this is exactly the 2n bound of the amortised argument) and n for the all-match case.
  On the plot the KMP point for the near miss at n = 100 000 is higher than the straight line (1.11 ms, the n = 50 000 point was 0.30 ms), although
  its operation count grows exactly linearly (99 901 → 199 901). I think this is measurement noise (garbage collection or JIT) in an average of only 5 runs,
  and not a real change of complexity, but I did not run the experiment again to check this.
- Rabin–Karp is fast on the near miss, because the hash of the last character always differs, so no window is ever verified (0 comparisons).
  But in the all-match case every window **really** matches, so every one of the 99 901 windows must be verified with m = 100 character comparisons: 9 990 100 comparisons,
  the same as Naive. This is the O(n·m) worst case of Rabin–Karp, and it is caused by real matches, not by collisions (the number of collisions is 0).
  It was still faster than Naive (5.9 ms vs. 15.3 ms); I think the verification loop is cheaper than the Naive loop, but I did not investigate this further.

**Kasai.** The number of character comparisons was 1 990, 9 987 and 19 989 for n = 1 000, 5 000 and 10 000, i.e. about 2n, which is consistent with the O(n) bound from the analysis (at most 3n). The time grew from 14.1 µs to 147.7 µs for a 10× longer string, a factor of 10.5, which agrees
with O(n). The suffix array construction (not timed) was much more expensive than Kasai itself.

**Measurement limits.** Every number is an average of only 5 runs, and times of tens of microseconds are sensitive to noise (JIT, garbage collector, other
programs). Small differences, for example Naive vs. KMP on random text, should not be trusted. Only large differences and growth trends are reliable.
Theoretical complexity and measured time are different things: the theory ignores constants, which is why Naive (worst case O(n·m)) is as fast as KMP
on random text, and why the algorithm with the best bound or the fewest operations (Rabin–Karp) is not automatically the fastest.

## 8. Conclusion

| Algorithm | Good for | Main trade-off |
|---|---|---|
| **LCS / Edit Distance** | comparing two sequences (diff tools, spell checking, DNA similarity) | Θ(n·m) time and, with the full table, Θ(n·m) memory; the memory can be reduced to O(min(n, m)) if the restoration of the answer is not needed. For two strings of length 1000 it is still fast (1.5–3 ms), but it does not scale to very long inputs. Edit Distance was about 1.8–2× slower than LCS here. |
| **Naive** | short texts and patterns, random text over a larger alphabet; simplest code | worst case O(n·m); very slow on repetitive texts (15 ms vs. 0.9 ms for KMP in my worst-case test) |
| **KMP** | guaranteed linear time, repetitive texts, streaming (the text pointer never goes back) | needs O(m) extra memory and a preprocessing step; on random text it gave no real speed-up over Naive |
| **Rabin–Karp** | many patterns at once, or when a hash is natural (plagiarism detection), simple window logic | expected O(n + m) but worst case O(n·m); slower constant because of modular arithmetic (about 2× slower here); needs verification because of collisions |
| **Kasai** | LCP array in linear time when a suffix array is already available (repeated substrings, counting distinct substrings, suffix-array based search) | needs the suffix array first (the simple builder I used is O(n² log n) in the worst case); extra O(n) memory for the rank array |

Main lessons from this assignment: (1) a dynamic programming table is easy to write once the recurrence and the base cases are clear, and the cost is
the size of the table; (2) the worst case of an algorithm can be very different from its typical behaviour: Naive and Rabin–Karp look good on random text and bad on
`AAAA…A`, while KMP is the same in every case; (3) a good theoretical bound and a small number of operations do not automatically mean the best measured time,
so both theory and experiments are needed.
