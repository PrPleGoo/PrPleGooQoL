package prplegoo.regions.api.npc.buildinglogic;

import game.boosting.BoostSpec;
import lombok.Getter;
import prplegoo.regions.api.MagicStringChecker;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import world.region.RD;
import world.region.building.RDBuildPoints;
import world.region.building.RDBuilding;

public class GeneticVariables {
    public static final int mutationAttemptsPerTick = 20;

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

        healthBuildingIndeces[buildingIndex] = 1;
        if (mutationNotAllowed(buildingIndex)
                || isGlobalBuilding(buildingIndex)) {
            return false;
        }

        LIST<BoostSpec> boosts = RD.BUILDINGS().all.get(buildingIndex).boosters().all();
        for (int i = 0; i < boosts.size(); i++) {
            BoostSpec boost = boosts.get(i);
            if (boost.boostable.key.equals("WORLD_HEALTH") && (!boost.booster.isMul && boost.booster.to() > 0 || boost.booster.isMul && boost.booster.to() > 1)) {
                healthBuildingIndeces[buildingIndex] = 2;
            }
            if (boost.boostable.key.startsWith("WORLD_POPULATION_CAPACITY") && (!boost.booster.isMul && boost.booster.to() < 0 || boost.booster.isMul && boost.booster.to() < 1)) {
                healthBuildingIndeces[buildingIndex] = 1;
                break;
            }
        }

        return healthBuildingIndeces[buildingIndex] == 2;
    }

    private static int[] loyaltyBuildingIndeces;
    @Getter
    private final static ArrayListGrower<Integer> actualLoyaltyBuildingIndeces = new ArrayListGrower<>();
    public static void isLoyaltyBuilding(int buildingIndex) {
        if (loyaltyBuildingIndeces == null) {
            loyaltyBuildingIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (loyaltyBuildingIndeces[buildingIndex] != 0) {
            return;
        }

        loyaltyBuildingIndeces[buildingIndex] = 1;
        if (mutationNotAllowed(buildingIndex)
            || isGlobalBuilding(buildingIndex)) {
            return;
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

        if (loyaltyBuildingIndeces[buildingIndex] == 2) {
            actualLoyaltyBuildingIndeces.add(buildingIndex);

        }

    }

    private static int[] workforceConsumerIndeces;
    public static boolean isWorforceConsumer(int buildingIndex) {
        if (workforceConsumerIndeces == null) {
            workforceConsumerIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (workforceConsumerIndeces[buildingIndex] != 0) {
            return workforceConsumerIndeces[buildingIndex] == 2;
        }

        workforceConsumerIndeces[buildingIndex] = 1;
        for (RDBuildPoints.RDBuildPoint cost : RD.BUILDINGS().costs.ALL){
            if(MagicStringChecker.isWorkforceBoostableKey(cost.bo.key)){
                workforceConsumerIndeces[buildingIndex] = 2;
                break;
            }
        }

        return workforceConsumerIndeces[buildingIndex] == 2;
    }

    public static boolean mutationNotAllowed(int buildingIndex) {
        return !RD.BUILDINGS().all.get(buildingIndex).AIBuild
                || GeneticVariables.isQuarantineBuilding(buildingIndex);
    }

    private static int[] globalBuildingIndeces;
    public static boolean isGlobalBuilding(int buildingIndex) {
        if (globalBuildingIndeces == null) {
            globalBuildingIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (globalBuildingIndeces[buildingIndex] != 0) {
            return globalBuildingIndeces[buildingIndex] == 2;
        }

        globalBuildingIndeces[buildingIndex] = 1;
        if (isGrowthBuilding(buildingIndex)
                || mutationNotAllowed(buildingIndex)
                || RD.BUILDINGS().all.get(buildingIndex).key().startsWith("RELIGION")) {
            return false;
        }

        ArrayListGrower<RDBuilding.BBoost> boosts = RD.BUILDINGS().all.get(buildingIndex).getBboosts();
        for (int i = 0; i < boosts.size(); i++) {
            RDBuilding.BBoost boost = boosts.get(i);
            if (boost.isGlobal()) {
                globalBuildingIndeces[buildingIndex] = 2;
                break;
            }
        }

        return globalBuildingIndeces[buildingIndex] == 2;
    }

    private static int[] moneyBuildingIndeces;
    public static boolean isMoneyBuilding(int buildingIndex) {
        if (moneyBuildingIndeces == null) {
            moneyBuildingIndeces = new int[RD.BUILDINGS().all.size()];
        }

        if (moneyBuildingIndeces[buildingIndex] != 0) {
            return moneyBuildingIndeces[buildingIndex] == 2;
        }

        moneyBuildingIndeces[buildingIndex] = 1;
        if (mutationNotAllowed(buildingIndex)) {
            return false;
        }

        LIST<BoostSpec> boosts = RD.BUILDINGS().all.get(buildingIndex).boosters().all();
        for (int i = 0; i < boosts.size(); i++) {
            BoostSpec boost = boosts.get(i);
            if (boost.boostable.key.startsWith("WORLD_TAX_INCOME")) {
                moneyBuildingIndeces[buildingIndex] = 2;
                break;
            }
        }

        return moneyBuildingIndeces[buildingIndex] == 2;
    }
}
