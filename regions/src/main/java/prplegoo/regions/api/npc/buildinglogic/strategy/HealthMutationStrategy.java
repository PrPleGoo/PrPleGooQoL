package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public class HealthMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;
        double health = RD.HEALTH().boostablee.get(region);

        if (GeneticVariables.isHealthBuilding(buildingGenetic.buildingIndex)) {
            if (health <= 1) {
                return tryLevelUpgrade(levelInt, buildingGenetic, region);
            }else {
                return tryLevelDowngrade(levelInt, buildingGenetic, region);
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