package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.GovPoints;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class PopulationGrowthMutationStrategy extends LoopingMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (GeneticVariables.isGrowthBuilding(buildingGenetic.buildingIndex)
                || GeneticVariables.isLoyaltyBuilding(buildingGenetic.buildingIndex)
                || GeneticVariables.isHealthBuilding(buildingGenetic.buildingIndex)) {
            return tryLevelChange(levelInt, buildingGenetic, region);
        }

        if (RND.oneIn(5)) {
            return tryLevelDowngrade(levelInt, buildingGenetic, region);
        }

        return false;
    }

    private boolean tryLevelChange(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        int rand = RND.rInt(5);

        if (rand == 0 && tryLevelDowngrade(levelInt, buildingGenetic, region)) {
            return true;
        }
        if (rand == 1) {
            return false;
        }
        if ((rand > 1) && tryLevelUpgrade(levelInt, buildingGenetic, region)) {
            return true;
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[4];
        fitnessRecords[0] = new GovPoints(faction, 0);
        fitnessRecords[1] = new Health(faction, 1);
        fitnessRecords[2] = new Loyalty(faction, 2);
        // PopTarget;
        fitnessRecords[3] = new FitnessRecord(faction, 3) {
            @Override
            public boolean willIncreaseDeficit(FactionNPC faction, FactionGenetic mutant) {
                return factionValue > mutant.fitnessRecords[index].factionValue;
            }

            @Override
            public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
                return factionValue < mutant.fitnessRecords[index].factionValue;
            }

            @Override
            public double determineValue(FactionNPC faction) {
                double popTarget = 0;

                for (Region region : faction.realm().all()) {
                    popTarget += RD.RACES().capacity.get(region);
                }

                return popTarget;
            }
        };

        return fitnessRecords;
    }
}