package prplegoo.regions.api;

import snake2d.util.misc.CLAMP;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

import java.util.HashMap;

public class RDWorkers {
    public static final int MIN_WORKERS = 1;
    public static final int MAX_WORKERS = 100;

    private final int[][] allocatedWorkers;

    public RDWorkers(RD.RDInit init) {
        allocatedWorkers = new int[WORLD.REGIONS().all().size()][RD.BUILDINGS().all.size()];

        for(int regionI = 0; regionI < WORLD.REGIONS().all().size(); regionI++)
        {
            Region region = WORLD.REGIONS().all().get(regionI);

            for(int buildingI = 0; buildingI < RD.BUILDINGS().all.size(); buildingI++)
            {
                RDBuilding building = RD.BUILDINGS().all.get(buildingI);

                set(region, building, 50);
            }
        }
    }

    public int getTotal(Region r) {
        int result = 0;
        for(int buildingI = 0; buildingI < allocatedWorkers[r.index()].length; buildingI++) {
            result += get(r, RD.BUILDINGS().all.get(buildingI));
        }

        return result;
    }

    public void set(Region region, RDBuilding building, int t) {
        allocatedWorkers[region.index()][building.index()] = CLAMP.i(t, MIN_WORKERS, MAX_WORKERS);
    }

    public int get(Region region, RDBuilding building) {
        if(building.isPopScaler && RD.BUILDINGS().tmp().level(building, region) != 0) {
            return allocatedWorkers[region.index()][building.index()];
        }
        return 0;
    }
}

