package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public abstract class BigMutationStrategy extends MutationStrategy {
    @Override
    protected boolean tryLevelUpgrade(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        boolean doneMutation = false;
        for (int i = 0; i < 5; i++) {
            if (super.tryLevelUpgrade(levelInt, buildingGenetic, region)) {
                doneMutation = true;
            } else {
                break;
            }
        }

        return doneMutation;
    }
}
