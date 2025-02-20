package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class IndustrializeMutationStrategy extends MutationStrategy {
    @Override
    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            int pick = RND.rInt(regionGenetic.buildingGenetics.length);
            if (tryMutateBuilding(regionGenetic.buildingGenetics[pick], region)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
            || buildingGenetic.recipe == -1) {
            return false;
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        if (building.getBlue() == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) building.getBlue()).industries();
        FactionNPC faction = (FactionNPC) region.faction();

        for (int i = 0; i < industries.size(); i++) {

            LIST<Industry.IndustryResource> inputs = industries.get(i).ins();
            boolean usesDefecit = false;
            for(int j = 0; j < inputs.size(); j++) {
                RESOURCE resource = inputs.get(j).resource;
                if (faction.stockpile.amount(resource) <= faction.stockpile.amTarget(resource)){
                    if (tryLevelDowngrade(building.level, buildingGenetic, region)) {
                        return true;
                    }

                    usesDefecit = true;
                    break;
                }
            }
            if(usesDefecit) {
                continue;
            }

            int pick = RND.rInt(industries.size());

            for (int j = 0; j < industries.get(i).outs().size(); j++) {

                RESOURCE resource = industries.get(pick).outs().get(j).resource;
                if (faction.stockpile.amount(resource) <= faction.stockpile.amTarget(resource)
                        && tryLevelUpgrade(building.level, buildingGenetic, region)) {

                    RD.RECIPES().setRecipe(region, building.getBlue(), pick);
                    buildingGenetic.recipe = pick;

                    return true;
                }
            }
        }

        return false;
    }
}
