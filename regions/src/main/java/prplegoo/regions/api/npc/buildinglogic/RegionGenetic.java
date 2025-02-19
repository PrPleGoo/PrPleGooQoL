package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import lombok.Getter;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RegionGenetic {
    private final int regionIndex;
    private final BuildingGenetic[] buildingGenetics;

    public RegionGenetic(int regionIndex) {
        this.regionIndex = regionIndex;
        buildingGenetics = new BuildingGenetic[RD.BUILDINGS().all.size()];
        for (int i = 0; i < buildingGenetics.length; i++) {
            buildingGenetics[i] = new BuildingGenetic(regionIndex, i);
        }
    }

    public boolean mutate() {
        Region region = WORLD.REGIONS().all().get(regionIndex);

        boolean didMutationOccur = false;
        for(int i = 0; i < buildingGenetics.length; i++) {
            didMutationOccur = didMutationOccur || buildingGenetics[RND.rInt(buildingGenetics.length)].mutate(region);
        }

        return didMutationOccur;
    }

    public void commit() {
        for (BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.commit(WORLD.REGIONS().all().get(regionIndex));
        }
    }
}
