"""
Makes the plots for the report from results/tables/results.csv
Run from the project root:  python3 results/plots/make_plots.py
"""
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

rows = []
with open("results/tables/results.csv") as f:
    for r in csv.DictReader(f):
        r["n"] = int(r["n"])
        r["avg_ns"] = float(r["avg_ns"])
        rows.append(r)


def series(experiment, algorithm, workload=None):
    pts = [(r["n"], r["avg_ns"]) for r in rows
           if r["experiment"] == experiment and r["algorithm"] == algorithm
           and (workload is None or r["workload"] == workload)]
    pts.sort()
    return [p[0] for p in pts], [p[1] for p in pts]


def reference(ax, xs, ys, power, label):
    # dashed line that grows like x^power and goes through the first measured point
    ref = [ys[0] * (x / xs[0]) ** power for x in xs]
    ax.plot(xs, ref, linestyle="--", color="gray", linewidth=1, label=label)


def style(ax, xlabel, title):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel(xlabel)
    ax.set_title(title)
    ax.grid(True, which="both", linestyle=":", alpha=0.6)


# ---------- Plot 1: LCS and edit distance
fig, ax = plt.subplots(figsize=(7, 5))
for name, marker in (("LCS", "o"), ("Edit Distance", "s")):
    xs, ys = series("DP", name)
    ax.plot(xs, ys, marker=marker, label=name)
reference(ax, *series("DP", "LCS"), 2, "n^2 reference")
style(ax, "String length n (both strings), log scale", "LCS and Edit Distance: time vs. input size")
ax.set_ylabel("Average time (ns), log scale")
ax.legend()
fig.tight_layout()
fig.savefig("results/plots/lcs_edit_time.png", dpi=150)
plt.close(fig)

# ---------- Plot 2: string matching on random text
fig, axes = plt.subplots(1, 2, figsize=(12, 5), sharey=True)
for ax, workload in zip(axes, ("pattern present", "pattern absent")):
    for name, marker in (("Naive", "o"), ("KMP", "s"), ("Rabin-Karp", "^")):
        xs, ys = series("Matching", name, workload)
        ax.plot(xs, ys, marker=marker, label=name)
    reference(ax, *series("Matching", "KMP", workload), 1, "n reference")
    style(ax, "Text length n, log scale", "Random text, " + workload + " (m = 20)")
    ax.legend()
axes[0].set_ylabel("Average time (ns), log scale")
fig.tight_layout()
fig.savefig("results/plots/matching_time.png", dpi=150)
plt.close(fig)

# ---------- Plot 3: worst-case workloads for matching
fig, axes = plt.subplots(1, 2, figsize=(12, 5), sharey=True)
for ax, workload in zip(axes, ("worst: near miss (A^99 C)", "worst: all match (A^100)")):
    for name, marker in (("Naive", "o"), ("KMP", "s"), ("Rabin-Karp", "^")):
        xs, ys = series("Matching", name, workload)
        ax.plot(xs, ys, marker=marker, label=name)
    reference(ax, *series("Matching", "KMP", workload), 1, "n reference")
    style(ax, "Text length n (text = AAA...A), log scale", workload + " (m = 100)")
    ax.legend()
axes[0].set_ylabel("Average time (ns), log scale")
fig.tight_layout()
fig.savefig("results/plots/matching_worst_case.png", dpi=150)
plt.close(fig)

# ---------- Plot 4: Kasai
fig, ax = plt.subplots(figsize=(7, 5))
xs, ys = series("Kasai", "Kasai (LCP)")
ax.plot(xs, ys, marker="o", label="Kasai")
reference(ax, xs, ys, 1, "n reference")
style(ax, "String length n, log scale", "Kasai's algorithm: time vs. string length")
ax.set_ylabel("Average time (ns), log scale")
ax.legend()
fig.tight_layout()
fig.savefig("results/plots/kasai_time.png", dpi=150)
plt.close(fig)

print("plots saved")
