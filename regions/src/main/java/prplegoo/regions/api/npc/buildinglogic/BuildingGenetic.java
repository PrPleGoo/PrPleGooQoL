package prplegoo.regions.api.npc.buildinglogic;

import game.boosting.BoostSpec;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Gen;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class BuildingGenetic {
    public final int buildingIndex;
    public int level;
    public int recipe;

    public BuildingGenetic(int regionIndex, int buildingIndex) {
        this.buildingIndex = buildingIndex;
        if (GeneticVariables.mutationNotAllowed(buildingIndex)) {
            return;
        }

        RDBuilding building = RD.BUILDINGS().all.get(this.buildingIndex);
        Region region = WORLD.REGIONS().all().get(regionIndex);

        this.level = building.level.get(region);

        RoomBlueprintImp blue = building.getBlue();
        if (blue instanceof INDUSTRY_HASER) {
            this.recipe = RD.RECIPES().getRecipeIndex(region, blue);
        } else {
            recipe = -1;
        }
    }

    public void commit(Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingIndex)) {
            return;
        }

        if (recipe != -1) {
            RD.RECIPES().setRecipe(region, RD.BUILDINGS().all.get(this.buildingIndex).getBlue(), recipe);
        }

        RD.BUILDINGS().all.get(buildingIndex).level.set(region, level);
    }
}
