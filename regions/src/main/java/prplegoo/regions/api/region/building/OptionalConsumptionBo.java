package prplegoo.regions.api.region.building;

import game.boosting.BSourceInfo;
import init.resources.RESOURCE;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.Creator;
import world.region.building.RDBuilding;

public class OptionalConsumptionBo extends Creator.Bo {
    private final int buildingIndex;
    private final int resourceIndex;

    public OptionalConsumptionBo(RDBuilding building, RESOURCE resource) {
        super(new BSourceInfo("Bonus input", resource.icon().small), 0, 15, false);

        this.buildingIndex = building.index();
        this.resourceIndex = resource.index();
    }

    @Override
    public double get(Region reg) {
        return RD.OPTIONAL_CONSUMPTION().isEnabled(reg, buildingIndex, resourceIndex) ? 1 : 0;
    }
}
