package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public abstract class BigMutationStrategy extends MutationStrategy {
    @Override
    protected boolean tryLevelUpgrade(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        boolean doneMutation = false;
        int loopsToDo = (int) (RD.SLAVERY().getWorkforce().bo.get(region) / 20);
        for (int i = 0; i < loopsToDo + 1; i++) {
            if (super.tryLevelUpgrade(levelInt, buildingGenetic, region)) {
                doneMutation = true;
            } else {
                break;
            }
        }

        return doneMutation;
    }
}
