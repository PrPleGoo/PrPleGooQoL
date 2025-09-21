package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.BuildingGenetic;
import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import world.WORLD;
import world.map.regions.Region;

import java.util.Arrays;

public abstract class LoopingMutationStrategy extends MutationStrategy {
    public boolean tryMutate(FactionGenetic factionGenetic) {
        return Arrays.stream(factionGenetic.getRegionGenetics())
                .map(this::mutateRegion)
                .reduce(false, (a, b) -> a | b); // didMutationOccur = false, |= mutateRegion
    }

    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        return Arrays.stream(regionGenetic.buildingGenetics)
                .map(building -> tryMutateBuilding(building, region))
                .reduce(false, (a, b) -> a | b); // didMutationOccur = false, |= tryMutateBuilding
    }
}