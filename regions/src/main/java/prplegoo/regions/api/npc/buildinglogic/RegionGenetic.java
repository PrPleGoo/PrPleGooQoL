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

    public void mutate() {
        Region region = WORLD.REGIONS().all().get(regionIndex);

        for(BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.mutate(region);
        }
    }

    public void commit() {
        for (BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.commit(WORLD.REGIONS().all().get(regionIndex));
        }
    }
}
