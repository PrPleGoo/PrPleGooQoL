package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class LoyaltyMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (GeneticVariables.isLoyaltyBuilding(buildingGenetic.buildingIndex)
            && RND.oneIn(2)) {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];
        fitnessRecords[0] = new Loyalty(faction, 0) {
            @Override
            public double determineValue(FactionNPC faction, Region region) {
                double amount = 0;

                for (int i = 0; i < RD.RACES().all.size(); i++) {
                    amount = Math.min(amount, RD.RACES().all.get(i).loyalty.target.get(region));
                }

                return amount;
            }
        };

        return fitnessRecords;
    }
}