package prplegoo.regions.api.npc.buildinglogic;

import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class BuildingGenetic {
    private final int buildingIndex;
    private int level;
    private int recipe;

    public BuildingGenetic(int regionIndex, int buildingIndex) {
        this.buildingIndex = buildingIndex;

        RDBuilding building = RD.BUILDINGS().all.get(this.buildingIndex);
        Region region = WORLD.REGIONS().all().get(regionIndex);

        this.level = building.level.get(region);

        RoomBlueprintImp blue = building.getBlue();
        if(blue instanceof INDUSTRY_HASER){
            this.recipe = RD.RECIPES().getRecipeIndex(region, blue);
        } else {
            recipe = -1;
        }
    }

    public void mutate(Region region, RegionGenetic.RegionDeficits deficits) {
        if (!RND.oneIn(GeneticVariables.buildingMutationChance)
            || deficits.getGovpointDeficit() < 0) {
            return;
        }

        double random = GeneticVariables.random();

        if (level > 0
            && random < -0.5) {
//            && (deficits.govpointDeficit(random)
//                || deficits.workforceDeficit(random))) {
            // downgrade the building
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingIndex).level;

            levelInt.set(region, levelInt.get(region) - 1);
            level = levelInt.get(region);

            return;
        }

        if (level < RD.BUILDINGS().all.get(buildingIndex).levels().size() - 1
                && random > 0.5) {
//                && (deficits.healthDeficit(random)
//                || deficits.raiderDeficit(random)
//                || deficits.workforceAbundance(random))) {
            // upgrade the building
            INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingIndex).level;

            levelInt.set(region, levelInt.get(region) + 1);
            level = levelInt.get(region);

            return;
        }

        if (recipe != -1
                && RND.oneIn(GeneticVariables.recipeMutationChance)) {
            INDUSTRY_HASER industry = (INDUSTRY_HASER) RD.BUILDINGS().all.get(this.buildingIndex).getBlue();

            int pick = RND.rInt(industry.industries().size());
            RD.RECIPES().setRecipe(region, (RoomBlueprintImp) industry, pick);
            recipe = pick;

            return;
        }
    }

    public void commit(Region region){
        if (recipe != -1) {
            RD.RECIPES().setRecipe(region, RD.BUILDINGS().all.get(this.buildingIndex).getBlue(), recipe);
        }

        RD.BUILDINGS().all.get(buildingIndex).level.set(region, level);
    }
}
