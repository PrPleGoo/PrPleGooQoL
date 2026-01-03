package prplegoo.regions.api.npc.buildinglogic.strategy;

import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.resources.RESOURCE;
import prplegoo.regions.api.gen.ProspectCache;
import prplegoo.regions.api.npc.KingLevels;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Money;
import prplegoo.regions.api.npc.buildinglogic.fitness.Workforce;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.Arrays;

public class PrimarySectorStrategy extends BigMutationStrategy {
    @Override
    public boolean tryMutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        FactionNPC faction = (FactionNPC) region.faction();

        boolean didMutationOccur = false;
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            BuildingGenetic buildingGenetic = regionGenetic.buildingGenetics[i];
            if (!isPrimarySector(buildingGenetic)) {
                continue;
            }

            RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);
            double profit = PrimarySectorProfitCache.getInstance().get(faction, i);
            double averageProfit = PrimarySectorProfitCache.getInstance().getAverage(faction);

            if (averageProfit > profit
                    && RND.rFloat() < profit / averageProfit) {
                didMutationOccur = didMutationOccur | tryLevelDowngrade(building.level, buildingGenetic, region);
            } else if (RND.rFloat() < profit / PrimarySectorProfitCache.getInstance().getMax(faction)) {
                didMutationOccur = didMutationOccur | tryLevelUpgrade(building.level, buildingGenetic, region);
            } else {
                didMutationOccur = didMutationOccur | tryLevelDowngrade(building.level, buildingGenetic, region);
            }
        }

        return didMutationOccur;
    }

    private static boolean isPrimarySector(BuildingGenetic buildingGenetic) {
        RDBuilding building = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex);

        return PrimarySectorProfitCache.isPrimarySector(building);
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        FitnessRecord[] fitnessRecords = new FitnessRecord[1];

        fitnessRecords[0] = new Workforce(faction, 0);

        return fitnessRecords;
    }
}
