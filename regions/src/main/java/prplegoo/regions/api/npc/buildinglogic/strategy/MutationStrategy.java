package prplegoo.regions.api.npc.buildinglogic.strategy;

import game.faction.npc.FactionNPC;
import prplegoo.regions.api.npc.buildinglogic.*;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.rnd.RND;
import util.data.INT_O;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public abstract class MutationStrategy {
    public boolean tryMutate(FactionGenetic factionGenetic) {
        boolean didMutationOccur = false;
        for(int i = 0; i < factionGenetic.regionGenetics.length; i++) {
            int pick = RND.rInt(factionGenetic.regionGenetics.length);
            didMutationOccur = didMutationOccur || mutateRegion(factionGenetic.regionGenetics[pick]);
        }

        return didMutationOccur;
    }

    public boolean mutateRegion(RegionGenetic regionGenetic) {
        Region region = WORLD.REGIONS().all().get(regionGenetic.regionIndex);

        boolean didMutationOccur = false;
        for(int i = 0; i < regionGenetic.buildingGenetics.length; i++) {
            int pick = RND.rInt(regionGenetic.buildingGenetics.length);
            didMutationOccur = didMutationOccur || tryMutateBuilding(regionGenetic.buildingGenetics[pick], region);
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

    public FitnessRecord[] loadFitness(FactionNPC faction) {
        return FactionGenetic.loadDefault(faction);
    }
}

