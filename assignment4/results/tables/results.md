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
