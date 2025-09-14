package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class RandomMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)) {
            return tryLevelDowngrade(RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level, buildingGenetic, region);
        }

        if (!RND.oneIn(GeneticVariables.buildingMutationChance)) {
            if (buildingGenetic.level > 0 && buildingGenetic.recipe != -1
                    && RND.oneIn(GeneticVariables.recipeMutationChance)) {
                INDUSTRY_HASER industry = (INDUSTRY_HASER) RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).getBlue();

                int pick = RND.rInt(industry.industries().size());
                RD.RECIPES().setRecipe(region, buildingGenetic.buildingIndex, (RoomBlueprintImp) industry, pick);
                buildingGenetic.recipe = pick;

                return true;
            }

            return false;
        }

        double currentWorkforce = RD.SLAVERY().getWorkforce().bo.get(region);
        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        boolean isGrowthBuilding = GeneticVariables.isGrowthBuilding(buildingGenetic.buildingIndex);

        if (!isGrowthBuilding && currentWorkforce < 0 && buildingGenetic.level > 0) {
            levelInt.set(region, levelInt.get(region) - 1);
            double newWorkforce = RD.SLAVERY().getWorkforce().bo.get(region);
            if (newWorkforce > currentWorkforce) {
                buildingGenetic.level--;
            }

            levelInt.set(region, buildingGenetic.level);
            return true;
        }

        double random = GeneticVariables.random();

        if (buildingGenetic.level < RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).levels().size() - 1
                && (random > 0.3 || (region.capitol() && isGrowthBuilding))) {
            // upgrade the building
            if (RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).canAfford(region, buildingGenetic.level, buildingGenetic.level + 1) == null) {
                if(buildingGenetic.level == 0 && buildingGenetic.recipe != -1) {
                    INDUSTRY_HASER industry = (INDUSTRY_HASER) RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).getBlue();

                    int pick = RND.rInt(industry.industries().size());
                    RD.RECIPES().setRecipe(region, buildingGenetic.buildingIndex, (RoomBlueprintImp) industry, pick);
                    buildingGenetic.recipe = pick;
                }

                levelInt.set(region, levelInt.get(region) + 1);
                buildingGenetic.level = levelInt.get(region);
                return true;
            }
        }

        if (!isGrowthBuilding
                && buildingGenetic.level > 0
                && random < -0.7) {
            // downgrade the building
            levelInt.set(region, levelInt.get(region) - 1);
            buildingGenetic.level = levelInt.get(region);

            return true;
        }

        return false;
    }
}
