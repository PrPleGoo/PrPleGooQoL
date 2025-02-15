package prplegoo.regions.api.npc.buildinglogic;

import game.boosting.BoostSpec;
import lombok.Getter;
import prplegoo.regions.api.MagicStringChecker;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class GeneticVariables {
    public static final int buildingMutationChance = 10;
    public static final double workForceValue = 100;
    public static final double healthValue = 5;
    public static final double govpointValue = 5;
    public static final int recipeMutationChance = 5;
    public static final int mutationAttemptsPerTick = 3;
    public static final double maxGovPointDeficit = -10;
    public static final int extraMutationsAfterReset = 3;

    public static double clamp(double value) {
        return CLAMP.d(value, -1, 1);
    }

    public static double random() {
        return RND.rFloat(2) - 1;
    }

    private static int growthBuildingIndex = -1;
    public static boolean isGrowthBuilding(Region region, int buildingIndex) {
        if(growthBuildingIndex != -1) {
            return growthBuildingIndex == buildingIndex;
        }

        if (MagicStringChecker.isGrowthBuilding(RD.BUILDINGS().all.get(buildingIndex).key())) {
            growthBuildingIndex = buildingIndex;
        }

        return false;
    }
}
