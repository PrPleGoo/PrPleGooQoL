package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class GlobalBuildingStrategy extends MutationStrategy {
    @Override
    public boolean tryMutate(FactionGenetic factionGenetic) {
        if (factionGenetic.getRegionGenetics().length >= 8) {
            return super.tryMutate(factionGenetic);
        }
        return false;
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || !GeneticVariables.isGlobalBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        return tryLevelUpgrade(levelInt, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
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
