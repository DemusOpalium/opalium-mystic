package de.opalium.dasloch.enchant;

import java.util.Collections;
import java.util.Map;

public final class EnchantEffects {

    private final Map<Integer, Integer> healPercentOnHit;
    private final Map<Integer, Integer> goldOnKill;
    private final Map<Integer, Integer> xpOnKillPercent;
    private final Map<Integer, Integer> streakBonusPercent;
    private final Map<Integer, Integer> bowExtraDamagePercent;
    private final int lastStandThresholdHearts;
    private final Map<Integer, Integer> lastStandReductionPercent;

    public EnchantEffects(Map<Integer, Integer> healPercentOnHit,
                          Map<Integer, Integer> goldOnKill,
                          Map<Integer, Integer> xpOnKillPercent,
                          Map<Integer, Integer> streakBonusPercent,
                          Map<Integer, Integer> bowExtraDamagePercent,
                          int lastStandThresholdHearts,
                          Map<Integer, Integer> lastStandReductionPercent) {
        this.healPercentOnHit = safe(healPercentOnHit);
        this.goldOnKill = safe(goldOnKill);
        this.xpOnKillPercent = safe(xpOnKillPercent);
        this.streakBonusPercent = safe(streakBonusPercent);
        this.bowExtraDamagePercent = safe(bowExtraDamagePercent);
        this.lastStandThresholdHearts = lastStandThresholdHearts;
        this.lastStandReductionPercent = safe(lastStandReductionPercent);
    }

    public Map<Integer, Integer> healPercentOnHit() {
        return healPercentOnHit;
    }

    public Map<Integer, Integer> goldOnKill() {
        return goldOnKill;
    }

    public Map<Integer, Integer> xpOnKillPercent() {
        return xpOnKillPercent;
    }

    public Map<Integer, Integer> streakBonusPercent() {
        return streakBonusPercent;
    }

    public Map<Integer, Integer> bowExtraDamagePercent() {
        return bowExtraDamagePercent;
    }

    public int lastStandThresholdHearts() {
        return lastStandThresholdHearts;
    }

    public Map<Integer, Integer> lastStandReductionPercent() {
        return lastStandReductionPercent;
    }

    private Map<Integer, Integer> safe(Map<Integer, Integer> map) {
        return map == null ? Collections.emptyMap() : Collections.unmodifiableMap(map);
    }
}
