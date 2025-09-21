package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public abstract class MutationStrategy {
    public boolean tryMutate(FactionGenetic factionGenetic) {
        boolean didMutationOccur = false;
        int randomIndex = RND.rInt(factionGenetic.getRegionGenetics().length);
        for(int i = 0; i < factionGenetic.getRegionGenetics().length; i++) {
            int actualIndex = (randomIndex + i) % factionGenetic.getRegionGenetics().length;
            didMutationOccur = didMutationOccur | mutateRegion(factionGenetic.getRegionGenetics()[actualIndex]);
        }

        return didMutationOccur;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        boolean didMutationOccur = false;
        int randomIndex = RND.rInt(regionGenetic.buildingGenetics.length);
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            int actualIndex = (randomIndex + i) % regionGenetic.buildingGenetics.length;
            didMutationOccur = didMutationOccur | tryMutateBuilding(regionGenetic.buildingGenetics[actualIndex], region);
        }

        return didMutationOccur;
    }

    public boolean tryMutateBuilding(BuildingGenetic buildingGenetic, Region region) { return false; }

    protected boolean tryLevelUpgrade(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        if (buildingGenetic.level < RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).levels().size() - 1
                && RD.BUILDINGS().all.get(buildingGenetic.buildingIndex).canAfford(region, buildingGenetic.level, buildingGenetic.level + 1) == null) {
            buildingGenetic.level += 1;
            levelInt.set(region, buildingGenetic.level);

            return true;
        }

        return false;
    }

    protected boolean tryLevelDowngrade(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        if (buildingGenetic.level > 0) {
            buildingGenetic.level -= 1;
            levelInt.set(region, buildingGenetic.level);

            return true;
        }

        return false;
    }

    protected boolean tryDestroyBuilding(INT_O.INT_OE<Region> levelInt, BuildingGenetic buildingGenetic, Region region) {
        if (buildingGenetic.level > 0) {
            buildingGenetic.level = 0;
            levelInt.set(region, buildingGenetic.level);

            return true;
        }

        return false;
    }

    public FitnessRecord[] loadFitness(FactionNPC faction) {
        return FactionGenetic.loadDefault(faction);
    }
}

