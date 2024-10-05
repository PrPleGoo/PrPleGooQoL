package prplegoo.regions.api;

import util.data.GETTER;
import util.data.INT;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class WorkerIntE implements INT.INTE {
    private final GETTER.GETTER_IMP<Region> region;
    private final RDBuilding building;

    public WorkerIntE(GETTER.GETTER_IMP<Region> region, RDBuilding building){
        this.region = region;
        this.building = building;
    }
    
    @Override
    public void set(int t) {
        RD.WORKERS().set(region.get(), building, t);
    }

    @Override
    public int get() {
        return RD.WORKERS().get(region.get(), building);
    }

    @Override
    public int min() {
        return RDWorkers.MIN_WORKERS;
    }

    @Override
    public int max() {
        return RDWorkers.MAX_WORKERS;
    }
}
