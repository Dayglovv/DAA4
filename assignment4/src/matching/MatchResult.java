package matching;

import java.util.List;

public class MatchResult {
    public final List<Integer> indices;
    public final long comparisons;
    public final long preprocessingOps;
    public final long hashUpdates;
    public final long falsePositives;

    public MatchResult(List<Integer> indices, long comparisons, long preprocessingOps,
                       long hashUpdates, long falsePositives) {
        this.indices = indices;
        this.comparisons = comparisons;
        this.preprocessingOps = preprocessingOps;
        this.hashUpdates = hashUpdates;
        this.falsePositives = falsePositives;
    }
}
