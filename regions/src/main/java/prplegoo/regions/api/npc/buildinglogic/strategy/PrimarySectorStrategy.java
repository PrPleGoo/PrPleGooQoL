package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.FitnessRecord;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.fitness.Money;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.misc.CLAMP;
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
        RoomBlueprintImp blue = building.getBlue();
        if (blue == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) blue).industries();
        FactionNPC faction = (FactionNPC) region.faction();

        if (industries.size() == 1 && industries.get(0).ins().isEmpty()) {
            LIST<IndustryResource> outputs = industries.get(0).outs();

            double outputCount = 0.0;
            double priceSum = 0.0;
            for (int j = 0; j < outputs.size(); j++) {
                RESOURCE resource = outputs.get(j).resource;

                double price = faction.stockpile.price.get(resource);

//                if (KingLevels.getInstance().getDailyProductionRate(faction, resource) < 0
//                        || KingLevels.getInstance().getDesiredStockpileAtNextLevel(faction, resource) > faction.stockpile.amount(resource)) {
//                    price *= 2;
//                }

                outputCount += outputs.get(j).rate;

                priceSum += outputs.get(j).rate * price / NPCStockpile.AVERAGE_PRICE;
            }

            double valueRate = priceSum / outputCount;

            double randomLow = 0.1 + RND.rFloat(0.4);
            double randomHigh = RND.rFloat(4.0) + 0.5;
            if (valueRate < randomLow) {
                return tryDestroyBuilding(building.level, buildingGenetic, region);
            } else if (valueRate * KingLevels.getInstance().getModifiedTechMul(building, (FactionNPC) region.faction()) > randomHigh) {
                return tryLevelUpgrade(building.level, buildingGenetic, region);
            }
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
