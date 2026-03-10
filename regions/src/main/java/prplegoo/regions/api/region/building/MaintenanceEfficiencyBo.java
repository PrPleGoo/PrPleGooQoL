package prplegoo.regions.api.region.building;

import game.boosting.BSourceInfo;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.Creator;
import world.region.building.RDBuilding;

public class MaintenanceEfficiencyBo extends Creator.Bo {
    private final int buildingIndex;
    private final int resourceIndex;
    private final boolean[] levels;

    public MaintenanceEfficiencyBo(RDBuilding building, int resourceIndex) {
        super(new BSourceInfo("Degrade: " + RESOURCES.ALL().get(resourceIndex).name, UI.icons().s.arrowUp), 0, 1, true);

        this.buildingIndex = building.index();
        this.resourceIndex = resourceIndex;

        this.levels = new boolean[building().getBlue().upgrades().max() + 1];
    }

    public void register(int level) {
        levels[level] = true;
    }

    private RDBuilding building() {
        return RD.BUILDINGS().all.get(buildingIndex);
    }

    @Override
    public double get(Region reg) {
        if (!levels[RD.UPGRADES().getLevel(reg, buildingIndex)]) {
            return 1;
        }

        return RD.DEFICITS().getDeficitModifier(resourceIndex);
    }
}

