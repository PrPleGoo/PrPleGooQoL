package prplegoo.regions.api.npc.buildinglogic;

import game.boosting.BoostSpec;
import prplegoo.regions.api.MagicStringChecker;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.region.RD;
import world.region.building.RDBuildPoints;

public class GeneticVariables {
    public static final int buildingMutationChance = 4;
    public static final int recipeMutationChance = 3;
    public static final int mutationAttemptsPerTick = 10;
    public static final int extraMutationsAfterReset = 10;

    public static double clamp(double value) {
        return CLAMP.d(value, -1, 1);
    }

    public static double random() {
        return RND.rFloat(2) - 1;
    }

    public static int growthBuildingIndex = -1;

    public static boolean isGrowthBuilding(int buildingIndex) {
        if (growthBuildingIndex != -1) {
            return growthBuildingIndex == buildingIndex;
        }

        if (MagicStringChecker.isGrowthBuilding(RD.BUILDINGS().all.get(buildingIndex).key())) {
            growthBuildingIndex = buildingIndex;
        }

        return false;
    }

    public static int quarantineBuildingIndex = -1;

    public static boolean isQuarantineBuilding(int buildingIndex) {
        if (quarantineBuildingIndex != -1) {
            return quarantineBuildingIndex == buildingIndex;
        }

        if (MagicStringChecker.isQuarantineBuilding(RD.BUILDINGS().all.get(buildingIndex).key())) {
            quarantineBuildingIndex = buildingIndex;
        }

        return false;
    }

    private static int[] healthBuildingIndeces;
    public static boolean isHealthBuilding(int buildingIndex) {
        if (healthBuildingIndeces == null) {
            healthBuildingIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (healthBuildingIndeces[buildingIndex] != 0) {
            return healthBuildingIndeces[buildingIndex] == 2;
        }

        LIST<BoostSpec> boosts = RD.BUILDINGS().all.get(buildingIndex).boosters().all();
        for (int i = 0; i < boosts.size(); i++) {
            BoostSpec boost = boosts.get(i);
            if (boost.boostable.key.equals("WORLD_HEALTH") && (!boost.booster.isMul && boost.booster.to() > 0 || boost.booster.isMul && boost.booster.to() > 1)) {
                healthBuildingIndeces[buildingIndex] = 2;
                continue;
            }
            if (boost.boostable.key.startsWith("WORLD_POPULATION_CAPACITY") && (!boost.booster.isMul && boost.booster.to() < 0 || boost.booster.isMul && boost.booster.to() < 1)) {
                healthBuildingIndeces[buildingIndex] = 1;
                break;
            }
        }

        if (healthBuildingIndeces[buildingIndex] == 0) {
            healthBuildingIndeces[buildingIndex] = 1;
        }

        return healthBuildingIndeces[buildingIndex] == 2;
    }

    private static int[] loyaltyBuildingIndeces;
    public static boolean isLoyaltyBuilding(int buildingIndex) {
        if (loyaltyBuildingIndeces == null) {
            loyaltyBuildingIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (loyaltyBuildingIndeces[buildingIndex] != 0) {
            return loyaltyBuildingIndeces[buildingIndex] == 2;
        }

        LIST<BoostSpec> boosts = RD.BUILDINGS().all.get(buildingIndex).boosters().all();
        for (int i = 0; i < boosts.size(); i++) {
            BoostSpec boost = boosts.get(i);
            if (boost.boostable.key.startsWith("WORLD_LOYALTY") && (!boost.booster.isMul && boost.booster.to() > 0 || boost.booster.isMul && boost.booster.to() > 1)) {
                loyaltyBuildingIndeces[buildingIndex] = 2;
                continue;
            }
            if (boost.boostable.key.startsWith("WORLD_POPULATION_CAPACITY") && (!boost.booster.isMul && boost.booster.to() < 0 || boost.booster.isMul && boost.booster.to() < 1)) {
                loyaltyBuildingIndeces[buildingIndex] = 1;
                break;
            }
        }

        if (loyaltyBuildingIndeces[buildingIndex] == 0) {
            loyaltyBuildingIndeces[buildingIndex] = 1;
        }

        return loyaltyBuildingIndeces[buildingIndex] == 2;
    }

    private static int[] workforceConsumerIndeces;
    public static boolean isWorforceConsumer(int buildingIndex) {
        if (workforceConsumerIndeces == null) {
            workforceConsumerIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (workforceConsumerIndeces[buildingIndex] != 0) {
            return workforceConsumerIndeces[buildingIndex] == 2;
        }

        for (RDBuildPoints.RDBuildPoint cost : RD.BUILDINGS().costs.ALL){
            if(MagicStringChecker.isWorkforceBoostableKey(cost.bo.key)){
                workforceConsumerIndeces[buildingIndex] = 2;
                break;
            }
        }

        if (workforceConsumerIndeces[buildingIndex] == 0) {
            workforceConsumerIndeces[buildingIndex] = 1;
        }

        return workforceConsumerIndeces[buildingIndex] == 2;
    }

    public static boolean mutationNotAllowed(int buildingIndex) {
        return !RD.BUILDINGS().all.get(buildingIndex).AIBuild
                || GeneticVariables.isQuarantineBuilding(buildingIndex);
    }
}
