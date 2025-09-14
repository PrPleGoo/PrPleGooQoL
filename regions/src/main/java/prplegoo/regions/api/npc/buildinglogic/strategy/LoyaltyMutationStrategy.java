package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Health;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public class LoyaltyMutationStrategy extends MutationStrategy {
    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        RD.OUTPUT().taxRate.set(region, 0);

        boolean canTryLoyaltyMutationStrategy = false;
        boolean anyMoreThanOne = false;
        for (int i = 0; i < RD.RACES().all.size(); i++) {
            RDRace race = RD.RACES().all.get(i);

            if (RD.RACES().edicts.massacre.toggled(race).get(region) == 1){
                continue;
            }

            if (race.loyalty.target.get(region) < 0) {
                canTryLoyaltyMutationStrategy = true;
            }
            if (race.loyalty.target.get(region) > 1) {
                anyMoreThanOne = true;
            }
        }

        if (!canTryLoyaltyMutationStrategy && !anyMoreThanOne) {
            return false;
        }

        boolean didMutationOccur = false;
        int randomIndex = RND.rInt(regionGenetic.buildingGenetics.length);
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            int actualIndex = (randomIndex + i) % regionGenetic.buildingGenetics.length;
            didMutationOccur = didMutationOccur || tryMutateBuilding(regionGenetic.buildingGenetics[actualIndex], region, canTryLoyaltyMutationStrategy, anyMoreThanOne);
        }

        return didMutationOccur;
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, boolean anyLessThanZero, boolean anyMoreThanOne) {
        if (!GeneticVariables.isLoyaltyBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;
        if (anyMoreThanOne && RND.oneIn(3) && tryLevelDowngrade(levelInt, buildingGenetic, region)) {
            return true;
        } else if (anyLessThanZero) {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Loyalty(faction, 0);

        return fitnessRecords;
    }
}