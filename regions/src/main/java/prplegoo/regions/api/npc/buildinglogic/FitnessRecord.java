package prplegoo.regions.api.npc.buildinglogic;

import game.faction.npc.FactionNPC;
import world.map.regions.Region;

public abstract class FitnessRecord {
    protected final int index;
    public double factionValue = 0;
    private final double[] regionValues;

    public FitnessRecord(FactionNPC faction, int index) {
        this.index = index;
        regionValues = new double[faction.realm().all().size()];
    }

    public double determineValue(FactionNPC faction, Region region) {
        return 0;
    }

    public double determineValue(FactionNPC faction) {
        return 0;
    }

    public double getFactionDeficitMax(FactionNPC faction) { return Double.NEGATIVE_INFINITY; }

    public double getRegionDeficitMax(FactionNPC faction) { return Double.NEGATIVE_INFINITY; }

    public boolean exceedsDeficit(FactionNPC faction) {
        // Both can be negative infinity, what do
        if (factionValue <= getFactionDeficitMax(faction)) {
//                LOG.ln("exceedsDeficit: " + index + ", factionValue: " + factionValue);
            return true;
        }

        for (int i = 0; i < regionValues.length; i++) {
            if (regionValues[i] <= getRegionDeficitMax(faction)) {
//                    LOG.ln("exceedsDeficit: " + index + ", regionValue: " + regionValue);
                return true;
            }
        }

        return false;
    }

    public void addValue(FactionNPC faction, int regionIndex) {
        regionValues[regionIndex] += determineValue(faction, faction.realm().all().get(regionIndex));
    }

    public void addValue(FactionNPC faction) {
        factionValue += determineValue(faction);
    }

    public boolean willIncreaseDeficit(FactionNPC faction, FactionGenetic mutant) {
        if (factionValue < 0) {
            return factionValue > mutant.fitnessRecords[index].factionValue;
        }
        if (mutant.fitnessRecords[index].factionValue < 0) {
            return true;
        }

        for (int i = 0; i < faction.realm().all().size(); i++) {
            if (regionValues[i] < 0) {
                return regionValues[i] > mutant.fitnessRecords[index].regionValues[i];
            }
            if (mutant.fitnessRecords[index].regionValues[i] < 0) {
                return true;
            }
        }

        return false;
    }

    public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
        if (mutant.fitnessRecords[index].factionValue != factionValue) {
            return (mutant.fitnessRecords[index].factionValue - factionValue) / factionValue > random;
        }
        for (int i = 0; i < faction.realm().all().size(); i++) {
            if (mutant.fitnessRecords[index].regionValues[i] != regionValues[i]
                    && (mutant.fitnessRecords[index].regionValues[i] - regionValues[i]) / regionValues[i] > random) {
                return true;
            }
        }

        return false;
    }
}
