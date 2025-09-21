package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

import java.util.stream.IntStream;

public class LoyaltyPruningMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutate(FactionGenetic factionGenetic) {
        int randomIndex = RND.rInt(factionGenetic.getRegionGenetics().length);

        return mutateRegion(factionGenetic.getRegionGenetics()[randomIndex]);
    }

    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        RD.OUTPUT().taxRate.set(region, 0);

        if (GeneticVariables.actualLoyaltyBuildingIndeces == null) {
            return false;
        }

        int randomIndex = RND.rInt(GeneticVariables.actualLoyaltyBuildingIndeces.size());

        return IntStream.range(0, GeneticVariables.actualLoyaltyBuildingIndeces.size())
                .map(i -> (randomIndex + i) % GeneticVariables.actualLoyaltyBuildingIndeces.size())
                .map(actualIndex -> GeneticVariables.actualLoyaltyBuildingIndeces.get(actualIndex))
                .anyMatch(buildingIndex -> tryMutateBuilding(regionGenetic.buildingGenetics[buildingIndex], region));
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        return tryLevelDowngrade(levelInt, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        return new FitnessRecord[]{
                new Loyalty(faction, 0)
        };
    }
}