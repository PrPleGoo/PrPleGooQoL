package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class ReduceDeficitMutationStrategy extends LoopingMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || buildingGenetic.recipe == -1) {
            return false;
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        if (building.getBlue() == null || building.level.get(region) == 0) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) building.getBlue()).industries();
        FactionNPC faction = (FactionNPC) region.faction();
        LIST<Industry.IndustryResource> inputs = industries.get(RD.RECIPES().getRecipeIndex(region, building.getBlue())).ins();

        for(int j = 0; j < inputs.size(); j++) {
            RESOURCE resource = inputs.get(j).resource;
            if (faction.stockpile.amount(resource) < 1
                && tryLevelDowngrade(building.level, buildingGenetic, region)) {
                return true;
            }
        }

        return false;
    }
}
