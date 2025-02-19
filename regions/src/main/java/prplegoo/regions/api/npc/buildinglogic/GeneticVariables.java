package prplegoo.regions.api.npc.buildinglogic;

import prplegoo.regions.api.MagicStringChecker;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.region.RD;

public class GeneticVariables {
    public static final int buildingMutationChance = 4;
    public static final int recipeMutationChance = 3;
    public static final int mutationAttemptsPerTick = 4;
    public static final int extraMutationsAfterReset = 3;
    public static final int regionMutationsPerMutation = 3;
    public static final int maxMutations = (mutationAttemptsPerTick + extraMutationsAfterReset) * regionMutationsPerMutation;

    public static double clamp(double value) {
        return CLAMP.d(value, -1, 1);
    }

    public static double random() {
        return RND.rFloat(2) - 1;
    }

    private static int growthBuildingIndex = -1;
    public static boolean isGrowthBuilding(int buildingIndex) {
        if(growthBuildingIndex != -1) {
            return growthBuildingIndex == buildingIndex;
        }

        if (MagicStringChecker.isGrowthBuilding(RD.BUILDINGS().all.get(buildingIndex).key())) {
            growthBuildingIndex = buildingIndex;
        }

        return false;
    }

    private static int quarantineBuildingIndex = -1;
    public static boolean isQuarantineBuilding(int buildingIndex) {
        if(quarantineBuildingIndex != -1) {
            return quarantineBuildingIndex == buildingIndex;
        }

        if (MagicStringChecker.isQuarantineBuilding(RD.BUILDINGS().all.get(buildingIndex).key())) {
            quarantineBuildingIndex = buildingIndex;
        }

        return false;
    }
}
