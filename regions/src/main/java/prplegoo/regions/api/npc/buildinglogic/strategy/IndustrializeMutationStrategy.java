package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
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
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class IndustrializeMutationStrategy extends BigMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || buildingGenetic.recipe == -1
                || buildingGenetic.level > 0) {
            return false;
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        RoomBlueprintImp blue = building.getBlue();
        if (blue == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) blue).industries();
        FactionNPC faction = (FactionNPC) region.faction();

        int randomIndex = RND.rInt(industries.size());
        double multiplier = KingLevels.getInstance().getModifiedTechMul(building, faction);
        main:
        for(int recipeIndex = 0; recipeIndex < industries.size(); recipeIndex++) {
            int actualIndex = (recipeIndex + randomIndex) % industries.size();

            LIST<Industry.IndustryResource> inputs = industries.get(actualIndex).ins();

            if (inputs.isEmpty()) {
                continue;
            }

            double inputPrice = 0.0;
            for (int j = 0; j < inputs.size(); j++) {
                RESOURCE resource = inputs.get(j).resource;

                if (faction.stockpile.amount(resource) < 1) {
                    continue main;
                }

                inputPrice += faction.stockpile.price.get(resource) * inputs.get(j).rate;
            }

            LIST<Industry.IndustryResource> outputs = industries.get(actualIndex).outs();

            double outputPrice = 0.0;
            for (int j = 0; j < outputs.size(); j++) {
                RESOURCE resource = outputs.get(j).resource;
                double ratePrice = faction.stockpile.price.get(resource) * outputs.get(j).rate;

//                if (KingLevels.getInstance().getDailyProductionRate(faction, resource) < 0
//                        || KingLevels.getInstance().getDesiredStockpileAtNextLevel(faction, resource) > faction.stockpile.amount(resource)) {
//                    ratePrice *= 2;
//                }

                outputPrice += ratePrice;
            }

            double margin = (outputPrice / inputPrice) - 1;
            boolean profitableRecipe = margin * multiplier > RND.rFloat(1.0);

            if (profitableRecipe
                    && tryLevelUpgrade(building.level, buildingGenetic, region)) {
                RD.RECIPES().setRecipe(region, buildingGenetic.buildingIndex, building.getBlue(), actualIndex);
                buildingGenetic.recipe = actualIndex;

                return true;
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
