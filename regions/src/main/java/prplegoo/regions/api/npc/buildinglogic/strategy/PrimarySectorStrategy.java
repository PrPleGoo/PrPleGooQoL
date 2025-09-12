package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class PrimarySectorStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
            || !RND.oneIn(4)) {
            return false;
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        if (building.getBlue() == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) building.getBlue()).industries();
        FactionNPC faction = (FactionNPC) region.faction();

        if (industries.size() == 1) {
            LIST<Industry.IndustryResource> outputs = industries.get(0).outs();

            double outputCount = 0.0;
            double priceSum = 0.0;
            for (int j = 0; j < outputs.size(); j++) {
                RESOURCE resource = outputs.get(j).resource;

                if (KingLevels.getInstance().getDailyProductionRate(faction, resource) < 0
                    && tryLevelUpgrade(building.level, buildingGenetic, region)) {
                        return true;
                }

                outputCount += outputs.get(j).rate;
                priceSum += outputs.get(j).rate * faction.stockpile.price.get(resource) / NPCStockpile.AVERAGE_PRICE;
            }

            double factoredPrice = priceSum / outputCount;

            double randomLow = RND.rFloat(0.1) + 0.1;
            double randomHigh = RND.rFloat(0.3) + 0.5;
            if (factoredPrice < randomLow && tryLevelDowngrade(building.level, buildingGenetic, region)) {
                return true;
            } else if (factoredPrice > randomHigh && tryLevelUpgrade(building.level, buildingGenetic, region)) {
                return true;
            }
        }

        return false;
    }
}
