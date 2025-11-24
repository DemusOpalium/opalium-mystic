package de.opalium.dasloch.well;

import java.util.Map;

public final class MysticWellTier {

    private final String id;
    private final int tokenMin;
    private final int tokenMax;
    private final Map<String, Integer> rareLimits;
    private final Map<String, Double> probabilities;

    public MysticWellTier(String id, int tokenMin, int tokenMax, Map<String, Integer> rareLimits, Map<String, Double> probabilities) {
        this.id = id;
        this.tokenMin = tokenMin;
        this.tokenMax = tokenMax;
        this.rareLimits = rareLimits;
        this.probabilities = probabilities;
    }

    public String id() {
        return id;
    }

    public int tokenMin() {
        return tokenMin;
    }

    public int tokenMax() {
        return tokenMax;
    }

    public Map<String, Integer> rareLimits() {
        return rareLimits;
    }

    public Map<String, Double> probabilities() {
        return probabilities;
    }
}
