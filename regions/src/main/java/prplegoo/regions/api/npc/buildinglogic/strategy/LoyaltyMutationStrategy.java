package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class LoyaltyMutationStrategy extends MutationStrategy {
    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        RD.OUTPUT().taxRate.set(region, 0);

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

        if (GeneticVariables.actualLoyaltyBuildingIndeces == null) {
            for (RDBuilding building : RD.BUILDINGS().all) {
                GeneticVariables.isLoyaltyBuilding(building.index());
            }
        }

        boolean didMutationOccur = false;
        int randomIndex = RND.rInt(GeneticVariables.actualLoyaltyBuildingIndeces.size());
        for(int i = 0; i < GeneticVariables.actualLoyaltyBuildingIndeces.size(); i++) {
            int actualIndex = (randomIndex + i) % GeneticVariables.actualLoyaltyBuildingIndeces.size();

            int buildingIndex = GeneticVariables.actualLoyaltyBuildingIndeces.get(actualIndex);
            didMutationOccur = didMutationOccur | tryMutateBuilding(regionGenetic.buildingGenetics[buildingIndex], region, anyLessThanZero, anyMoreThanZero);
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
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Loyalty(faction, 0);

        return fitnessRecords;
    }
}