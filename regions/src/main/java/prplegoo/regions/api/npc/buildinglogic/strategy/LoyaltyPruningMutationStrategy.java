package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public class LoyaltyPruningMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutate(FactionGenetic factionGenetic) {
        int randomIndex = RND.rInt(factionGenetic.regionGenetics.length);

        return mutateRegion(factionGenetic.regionGenetics[randomIndex]);
    }

    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        RD.OUTPUT().taxRate.set(region, 0);

        if (GeneticVariables.actualLoyaltyBuildingIndeces == null) {
            return false;
        }

        int randomIndex = RND.rInt(GeneticVariables.actualLoyaltyBuildingIndeces.size());
        for(int i = 0; i < GeneticVariables.actualLoyaltyBuildingIndeces.size(); i++) {
            int actualIndex = (randomIndex + i) % GeneticVariables.actualLoyaltyBuildingIndeces.size();

            int buildingIndex = GeneticVariables.actualLoyaltyBuildingIndeces.get(actualIndex);
            if (tryMutateBuilding(regionGenetic.buildingGenetics[buildingIndex], region)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        return tryLevelDowngrade(levelInt, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
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