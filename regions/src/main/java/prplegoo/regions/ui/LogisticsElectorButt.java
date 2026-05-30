package prplegoo.regions.ui;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import prplegoo.regions.api.region.rd.RDLogistics;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.clickable.CLICKABLE;
import util.colors.GCOLOR;
import util.data.GETTER;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class LogisticsElectorButt extends CLICKABLE.ClickableAbs {
    private final GETTER.GETTER_IMP<Region> g;
    private final int logisticsBuildingIndex;
    private final int resourceIndex;

    public LogisticsElectorButt(GETTER.GETTER_IMP<Region> g, RDBuilding bu, int resourceIndex) {
        body.setDim(32, 32);
        this.g = g;
        this.logisticsBuildingIndex = RDLogistics.getIndex(bu);
        this.resourceIndex = resourceIndex;
    }

    @Override
    protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
        GCOLOR.UI().border().render(r, body,-1);

        if (RD.LOGISTICS().isElected(g.get().index(), logisticsBuildingIndex, resourceIndex)) {
            COLOR.WHITE100.render(r, body,-2);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
        } else {
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
        }

        if (resourceIndex == -1) {
            UI.icons().m.b_stop.renderCY(r, body().x1()+4, body().cY());
        } else {
            RESOURCES.ALL().get(resourceIndex).icon().medium.renderCY(r, body().x1()+4, body().cY());
        }
    }

    @Override
    protected void clickA() {
        RD.LOGISTICS().elect(g.get().index(), logisticsBuildingIndex, resourceIndex);
    }
}
