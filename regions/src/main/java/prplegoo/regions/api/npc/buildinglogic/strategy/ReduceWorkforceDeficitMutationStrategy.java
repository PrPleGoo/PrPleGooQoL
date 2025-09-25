package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import world.WORLD;
import world.map.regions.Region;
import world.region.Gen;
import world.region.RD;

public class ReduceWorkforceDeficitMutationStrategy extends LoopingMutationStrategy {
    @Override
    public boolean tryMutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        if (RD.SLAVERY().getWorkforce().bo.get(region) >= 0) {
            return false;
        }

        return super.tryMutateRegion(regionGenetic);
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        if (GeneticVariables.isWorforceConsumer(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Workforce(faction, 0);

        return fitnessRecords;
    }
}
