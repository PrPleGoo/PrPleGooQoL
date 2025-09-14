package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
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
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class ReduceStorageMutationStrategy extends MutationStrategy {
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

        int randomIndex = RND.rInt(industries.size());
        main:
        for(int recipeIndex = 0; recipeIndex < industries.size(); recipeIndex++) {
            int actualIndex = (recipeIndex + randomIndex) % industries.size();

            LIST<Industry.IndustryResource> inputs = industries.get(actualIndex).ins();

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
                outputPrice += faction.stockpile.price.get(resource) * outputs.get(j).rate;
            }

            double margin = outputPrice / inputPrice;
            boolean profitableRecipe = margin > RND.rFloat(1.0) + 1.0;

            if (profitableRecipe) {
                RD.RECIPES().setRecipe(region, building.getBlue(), actualIndex);
                buildingGenetic.recipe = actualIndex;

                tryLevelUpgrade(building.level, buildingGenetic, region);

                return true;
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
