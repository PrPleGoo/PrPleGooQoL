package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class HealthImprovementStrategy extends MutationStrategy {
    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        boolean didMutationOccur = false;
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            didMutationOccur = didMutationOccur | tryMutateBuilding(regionGenetic.buildingGenetics[i], region);
        }

        return didMutationOccur;
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || !GeneticVariables.isHealthBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (RD.HEALTH().boostablee.get(region) <= 0.5) {
            if (tryLevelUpgrade(levelInt, buildingGenetic, region)){
                while(RD.HEALTH().boostablee.get(region) <= 0.5
                        && tryLevelUpgrade(levelInt, buildingGenetic, region)) {
                    // NOP
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Health(faction, 0);

        return fitnessRecords;
    }
}