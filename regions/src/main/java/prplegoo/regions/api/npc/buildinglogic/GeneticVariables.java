package prplegoo.regions.api.npc.buildinglogic;

import lombok.Getter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

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
}
