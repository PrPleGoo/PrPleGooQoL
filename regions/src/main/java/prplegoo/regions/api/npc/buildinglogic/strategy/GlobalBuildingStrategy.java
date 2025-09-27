package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class GlobalBuildingStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || GeneticVariables.isGrowthBuilding(buildingGenetic.buildingIndex)
                || !GeneticVariables.isGlobalBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (region.faction().realm().regions() < 12) {
            return tryLevelDowngrade(levelInt, buildingGenetic, region);
        } else {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        }
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        return new FitnessRecord[] {
                new FitnessRecord(faction, 0){
                    @Override
                    public boolean tryMutation(FactionNPC faction1, FactionGenetic mutant, double random) {
                        return true;
                    }
                }
        };
    }
}

