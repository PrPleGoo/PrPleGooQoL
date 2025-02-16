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
    private final int buildingIndex;
    private int level;
    private int recipe;

    public BuildingGenetic(int regionIndex, int buildingIndex) {
        this.buildingIndex = buildingIndex;
        if (!RD.BUILDINGS().all.get(buildingIndex).AIBuild) {
            return;
        }

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

    public void mutate(Region region) {
        if (!RD.BUILDINGS().all.get(buildingIndex).AIBuild) {
            return;
        }

        if (!RND.oneIn(GeneticVariables.buildingMutationChance)) {
            if (recipe != -1
                    && RND.oneIn(GeneticVariables.recipeMutationChance)) {
                INDUSTRY_HASER industry = (INDUSTRY_HASER) RD.BUILDINGS().all.get(this.buildingIndex).getBlue();

                int pick = RND.rInt(industry.industries().size());
                RD.RECIPES().setRecipe(region, (RoomBlueprintImp) industry, pick);
                recipe = pick;
            }

            return;
        }

        double currentWorkforce = RD.SLAVERY().getWorkforce().bo.get(region);
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingIndex).level;

        boolean isGrowthBuilding = GeneticVariables.isGrowthBuilding(buildingIndex);

        if (!isGrowthBuilding && currentWorkforce < 0 && level > 0) {
            levelInt.set(region, levelInt.get(region) - 1);
            double newWorkforce = RD.SLAVERY().getWorkforce().bo.get(region);
            if (newWorkforce > currentWorkforce) {
                level--;
                return;
            }

            levelInt.set(region, level);
        }

        double random = GeneticVariables.random();

        if (level < RD.BUILDINGS().all.get(buildingIndex).levels().size() - 1
                && (random > 0.3 || (region.capitol() && isGrowthBuilding))) {
            // upgrade the building
            if (RD.BUILDINGS().all.get(buildingIndex).canAfford(region, level, level + 1) == null) {
                levelInt.set(region, levelInt.get(region) + 1);
                level = levelInt.get(region);

                return;
            }
        }

        if (!isGrowthBuilding
                && level > 0
                && random < -0.7) {
            // downgrade the building
            levelInt.set(region, levelInt.get(region) - 1);
            level = levelInt.get(region);

            return;
        }
    }

    public void commit(Region region){
        if (!RD.BUILDINGS().all.get(buildingIndex).AIBuild) {
            return;
        }

        if (recipe != -1) {
            RD.RECIPES().setRecipe(region, RD.BUILDINGS().all.get(this.buildingIndex).getBlue(), recipe);
        }

        RD.BUILDINGS().all.get(buildingIndex).level.set(region, level);
    }
}
