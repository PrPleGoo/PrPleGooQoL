package prplegoo.regions.api.region.building;

import game.boosting.BSourceInfo;
import init.sprite.UI.UI;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.Creator;
import world.region.building.RDBuilding;

public class UpgradeBo extends Creator.Bo {
    private final int buildingIndex;

    public UpgradeBo(RDBuilding building) {
        super(new BSourceInfo("Building upgrade", UI.icons().s.arrowUp), 0, 15, false);

        this.buildingIndex = building.index();
    }

    private RDBuilding building() {
        return RD.BUILDINGS().all.get(buildingIndex);
    }

    @Override
    public double get(Region reg) {
        return building().getBlue().upgrades().boost(RD.UPGRADES().getLevel(reg, buildingIndex));
    }
}
