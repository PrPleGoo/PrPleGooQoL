package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class ReduceWorkforceDeficitMutationStrategy extends LoopingMutationStrategy {
    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        if (RD.SLAVERY().getWorkforce().bo.get(region) >= 0) {
            return false;
        }

        return super.mutateRegion(regionGenetic);
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        if (GeneticVariables.isWorforceConsumer(buildingGenetic.buildingIndex)
            && tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region)) {
            return true;
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Workforce(faction, 0) {
            @Override
            public double getRegionDeficitMax(FactionNPC faction) { return Double.NEGATIVE_INFINITY; }
        };

        return fitnessRecords;
    }
}
