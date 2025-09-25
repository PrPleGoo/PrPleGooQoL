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

public class RemoveBadRecipeMutationStrategy extends BigMutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || buildingGenetic.recipe == -1
                || buildingGenetic.level < 1) {
            return false;
        }

        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
        RoomBlueprintImp blue = building.getBlue();
        if (blue == null) {
            return false;
        }

        LIST<Industry> industries = ((INDUSTRY_HASER) blue).industries();

        LIST<Industry.IndustryResource> inputs = industries.get(buildingGenetic.recipe).ins();
        if (inputs.isEmpty()) {
            return false;
        }

        FactionNPC faction = (FactionNPC) region.faction();

        double inputPrice = 0.0;
        for (int j = 0; j < inputs.size(); j++) {
            RESOURCE resource = inputs.get(j).resource;

            inputPrice += faction.stockpile.price.get(resource) * inputs.get(j).rate;
        }

        LIST<Industry.IndustryResource> outputs = industries.get(buildingGenetic.recipe).outs();

        double outputPrice = 0.0;
        for (int j = 0; j < outputs.size(); j++) {
            RESOURCE resource = outputs.get(j).resource;
            double ratePrice = faction.stockpile.price.get(resource) * outputs.get(j).rate;

            outputPrice += ratePrice;
        }

        double margin = (outputPrice / inputPrice) - 1;
        boolean isMarginTooLow = margin < RND.rFloat(0.25);

        if (isMarginTooLow) {
            return tryDestroyBuilding(building.level, buildingGenetic, region);
        }

        return tryLevelUpgrade(building.level, buildingGenetic, region);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new FitnessRecord(faction, 0){
            @Override
            public boolean tryMutation(FactionNPC faction, FactionGenetic mutant, double random) {
                return true;
            }
        };

        return fitnessRecords;
    }
}
