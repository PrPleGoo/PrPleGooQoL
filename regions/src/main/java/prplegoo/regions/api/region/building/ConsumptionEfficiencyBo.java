package prplegoo.regions.api.region.building;

import game.boosting.BSourceInfo;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.Creator;
import world.region.building.RDBuilding;

public class ConsumptionEfficiencyBo extends Creator.Bo {
    private final int buildingIndex;
    private final int resourceIndex;
    private final boolean[] levels;

    public ConsumptionEfficiencyBo(RDBuilding building, int resourceIndex) {
        super(new BSourceInfo("Lacking: " + RESOURCES.ALL().get(resourceIndex).name, UI.icons().s.admin), 0, 1, true);

        this.buildingIndex = building.index();
        this.resourceIndex = resourceIndex;

        this.levels = new boolean[building().levels.size()];
    }

    public void register(int level) {
        levels[level] = true;
    }

    private RDBuilding building() {
        return RD.BUILDINGS().all.get(buildingIndex);
    }

    @Override
    public double get(Region reg) {
        if (!levels[building().level.get(reg)]) {
            return 1;
        }

        return RD.DEFICITS().getDeficitModifier(resourceIndex);
    }
}
