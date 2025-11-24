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

    public EnchantEffects(Map<Integer, Integer> healPercentOnHit, Map<Integer, Integer> goldOnKill,
                          Map<Integer, Integer> xpOnKillPercent, Map<Integer, Integer> streakBonusPercent,
                          Map<Integer, Integer> bowExtraDamagePercent, int lastStandThresholdHearts,
                          Map<Integer, Integer> lastStandReductionPercent) {
        this.healPercentOnHit = healPercentOnHit;
        this.goldOnKill = goldOnKill;
        this.xpOnKillPercent = xpOnKillPercent;
        this.streakBonusPercent = streakBonusPercent;
        this.bowExtraDamagePercent = bowExtraDamagePercent;
        this.lastStandThresholdHearts = lastStandThresholdHearts;
        this.lastStandReductionPercent = lastStandReductionPercent;
    }

    public Map<Integer, Integer> healPercentOnHit() {
        return safe(healPercentOnHit);
    }

    public Map<Integer, Integer> goldOnKill() {
        return safe(goldOnKill);
    }

    public Map<Integer, Integer> xpOnKillPercent() {
        return safe(xpOnKillPercent);
    }

    public Map<Integer, Integer> streakBonusPercent() {
        return safe(streakBonusPercent);
    }

    public Map<Integer, Integer> bowExtraDamagePercent() {
        return safe(bowExtraDamagePercent);
    }

    public int lastStandThresholdHearts() {
        return lastStandThresholdHearts;
    }

    public Map<Integer, Integer> lastStandReductionPercent() {
        return safe(lastStandReductionPercent);
    }

    private Map<Integer, Integer> safe(Map<Integer, Integer> map) {
        return map == null ? Collections.emptyMap() : map;
    }
}
