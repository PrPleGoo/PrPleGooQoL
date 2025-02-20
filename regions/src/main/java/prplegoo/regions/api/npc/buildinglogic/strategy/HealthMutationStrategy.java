package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class HealthMutationStrategy extends LoopingMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (GeneticVariables.isHealthBuilding(buildingGenetic.buildingIndex)) {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        }

        if (!GeneticVariables.isGrowthBuilding(buildingGenetic.buildingIndex)
                && RND.oneIn(GeneticVariables.buildingMutationChance * 2)
                && tryLevelDowngrade(levelInt, buildingGenetic, region)) {
            return true;
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];
        fitnessRecords[0] = new Health(faction, 0) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                return RD.HEALTH().boostablee.get(region) - 0.5;
            }
        };

        return fitnessRecords;
    }
}