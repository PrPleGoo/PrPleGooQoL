package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public abstract class BigMutationStrategy extends MutationStrategy {
    @Override
    protected boolean tryLevelUpgrade(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        if (buildingGenetic.level < RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).levels().size() - 1
                && RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).canAfford(region, buildingGenetic.level, buildingGenetic.level + 1) == null) {
            buildingGenetic.level += 1;
            levelInt.set(region, buildingGenetic.level);

            boolean hasLotsOfWorkforce = RD.SLAVERY().getWorkforce().bo.get(region) > 40;
            if (hasLotsOfWorkforce) {
                return true | tryLevelUpgrade(levelInt, buildingGenetic, region);
            }
            
            return true;
        }

        return false;
    }
}
