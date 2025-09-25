package prplegoo.regions.api.npc.buildinglogic;

import game.GAME;
import lombok.Getter;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

public class RegionGenetic {
    public final int regionIndex;
    public final BuildingGenetic[] buildingGenetics;
    public int taxRate;

    public RegionGenetic(int regionIndex) {
        this.regionIndex = regionIndex;
        buildingGenetics = new BuildingGenetic[RD.BUILDINGS().all.size()];
        for (int i = 0; i < buildingGenetics.length; i++) {
            buildingGenetics[i] = new BuildingGenetic(regionIndex, i);
        }

        taxRate = RD.OUTPUT().taxRate.get(WORLD.REGIONS().all().get(regionIndex));
    }

    public void commit() {
        for (BuildingGenetic buildingGenetic : buildingGenetics) {
            buildingGenetic.commit(WORLD.REGIONS().all().get(regionIndex));
        }

        RD.OUTPUT().taxRate.set(WORLD.REGIONS().all().get(regionIndex), taxRate);
    }
}
