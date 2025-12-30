package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.GeneticVariables;
import util.data.INT_O;
import world.map.regions.Region;
import world.region.RD;

public class ProtectionMutationStrategy extends MutationStrategy {
    @Override
    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) {
        if (!GeneticVariables.isFortificationBuilding(buildingGenetic.buildingIndex)
                && !GeneticVariables.isGarrisonBuilding(buildingGenetic.buildingIndex)) {
            return false;
        }

        INT_O.INT_OE<Region> levelInt = RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).level;
        double regionCapacity = RD.RACES().popTarget.getValue(region);
        int tryLevel = (int)(regionCapacity / 1500.0);

        if (levelInt.get(region) < tryLevel) {
            return tryLevelUpgrade(levelInt, buildingGenetic, region);
        }

        return false;
    }
}
