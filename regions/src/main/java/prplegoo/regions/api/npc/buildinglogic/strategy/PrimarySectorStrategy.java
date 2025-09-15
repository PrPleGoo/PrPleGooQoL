package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.fitness.Money;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrimarySectorStrategy extends BigMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        if (building.getBlue() == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) building.getBlue()).industries();
        FactionNPC faction = (FactionNPC) region.faction();

        if (industries.size() == 1 && industries.get(0).ins().isEmpty()) {
            LIST<Industry.IndustryResource> outputs = industries.get(0).outs();

            double outputCount = 0.0;
            double priceSum = 0.0;
            for (int j = 0; j < outputs.size(); j++) {
                RESOURCE resource = outputs.get(j).resource;

                if (KingLevels.getInstance().getDailyProductionRate(faction, resource) < 0) {
                    return tryLevelUpgrade(building.level, buildingGenetic, region);
                }

                outputCount += outputs.get(j).rate;

                double price = faction.stockpile.price.get(resource);
                priceSum += outputs.get(j).rate * price / NPCStockpile.AVERAGE_PRICE;
            }

            double factoredPrice = priceSum / outputCount;

            double randomLow = RND.rFloat(1.5);
            double randomHigh = RND.rFloat(4.0) + 1.5;
            if (factoredPrice < randomLow) {
                return tryLevelDowngrade(building.level, buildingGenetic, region);
            } else if (factoredPrice > randomHigh) {
                return tryLevelUpgrade(building.level, buildingGenetic, region);
            }
        }

        return false;
    }

    @Override
    public FitnessRecord[] loadFitness(FactionNPC faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[2];

        fitnessRecords[0] = new Workforce(faction, 0);
        fitnessRecords[1] = new Money(faction, 1);

        return fitnessRecords;
    }
}
