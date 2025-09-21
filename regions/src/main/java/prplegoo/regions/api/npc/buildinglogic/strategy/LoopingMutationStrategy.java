package prplegoo.regions.api.npc.buildinglogic.strategy;

import prplegoo.regions.api.npc.buildinglogic.FactionGenetic;
import prplegoo.regions.api.npc.buildinglogic.RegionGenetic;
import world.WORLD;
import world.map.regions.Region;

public abstract class LoopingMutationStrategy extends MutationStrategy {
    public boolean tryMutate(FactionGenetic factionGenetic) {
        boolean didMutationOccur = false;
        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            didMutationOccur = didMutationOccur | mutateRegion(factionGenetic.getRegionGenetics()[i]);
        }

        return didMutationOccur;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        boolean didMutationOccur = false;
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            didMutationOccur = didMutationOccur | tryMutateBuilding(regionGenetic.buildingGenetics[i], region);
        }

        return didMutationOccur;
    }
}
