package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import prplegoo.regions.api.npc.buildinglogic.fitness.Loyalty;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.Gen;
import world.region.RD;

import java.util.Arrays;

public class MoneyStrategy extends LoopingMutationStrategy {
    private static final double targetValue = 0.02;
    @Override
    public boolean tryMutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);
        FactionNPC faction = (FactionNPC) region.faction();

        double valueOfGoods = faction.stockpile.valueOfStockpile();
        double denari = faction.credits().getD();

        boolean needMoney = denari / valueOfGoods < targetValue;

        if (RND.oneIn(2)) {
            return needMoney
                    ? tryIncreaseTax(regionGenetic, region)
                    : tryDecreaseTax(regionGenetic, region);
        } else {
            return Arrays.stream(regionGenetic.buildingGenetics)
                    .map(buildingGenetic -> tryMutateBuilding(buildingGenetic, region, needMoney))
                    .reduce(false, (didOccur, tryMutateBuildingResult) -> didOccur | tryMutateBuildingResult);
        }
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region, boolean needMoney) {
        if (GeneticVariables.mutationNotAllowed(buildingGenetic.buildingIndex)
                || GeneticVariables.isGlobalBuilding(buildingGenetic.buildingIndex)
                || !GeneticVariables.isMoneyBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;

        if (needMoney) {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        } else {
            return tryLevelDowngrade(levelInt, buildingGenetic, region);
        }
    }

    @Override
    public FitnessRecord[] loadFitness(FactionGenetic faction) {
        return new FitnessRecord[] {
                new Loyalty(faction, 0),
        };
    }
}
