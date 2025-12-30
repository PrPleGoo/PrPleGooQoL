package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class LoyaltyMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        boolean anyLessThanZero = false;
        boolean anyMoreThanZero = false;
        for (int i = 0; i < RD.RACES().all.size(); i++) {
            RDRace race = RD.RACES().all.get(i);

            if (RD.RACES().edicts.massacre.toggled(race).get(region) == 1){
                continue;
            }

            double targetLoyalty = race.loyalty.target.get(region);
            if (targetLoyalty <= -0.1) {
                anyLessThanZero = true;
                break;
            }
            if (targetLoyalty > 0.1) {
                anyMoreThanZero = true;
            }
        }

        if (!anyLessThanZero && !anyMoreThanZero) {
            return false;
        }

        ArrayListGrower<Integer> actualLoyaltyBuildingIndeces = GeneticVariables.getActualLoyaltyBuildingIndeces();
        if (actualLoyaltyBuildingIndeces.isEmpty()) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                GeneticVariables.isLoyaltyBuilding(building.index());
            }
        }

        boolean didMutationOccur = false;
        int buildingIndecesSize = actualLoyaltyBuildingIndeces.size();
        int randomIndex = RND.rInt(buildingIndecesSize);
        for(int i = 0; i < buildingIndecesSize; i++) {
            int actualIndex = (randomIndex + i) % buildingIndecesSize;

            int buildingIndex = actualLoyaltyBuildingIndeces.get(actualIndex);
            didMutationOccur |= tryMutateBuilding(regionGenetic.buildingGenetics[buildingIndex], region, anyLessThanZero, anyMoreThanZero);
        }

        return didMutationOccur;
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, boolean anyLessThanZero, boolean anyMoreThanOne) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;
        if (anyLessThanZero){
            return tryLevelUpgrade(levelInt, buildingGenetic, region)
                    | tryLevelUpgrade(levelInt, buildingGenetic, region)
                    | tryLevelUpgrade(levelInt, buildingGenetic, region);
        } else if (anyMoreThanOne && RND.oneIn(3)) {
            return tryLevelDowngrade(levelInt, buildingGenetic, region);
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Loyalty(faction, 0){
            @Override
            public boolean exceedsDeficit(FactionNPC faction) {
                return false;
            }
        };

        return fitnessRecords;
    }
}

