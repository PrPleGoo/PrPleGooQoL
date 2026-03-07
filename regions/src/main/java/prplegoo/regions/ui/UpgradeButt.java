package prplegoo.regions.ui;

import game.faction.FACTIONS;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import util.colors.GCOLOR;
import util.colors.GCOLOR_TEXT;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class UpgradeButt extends CLICKABLE.ClickableAbs {
    private final GETTER.GETTER_IMP<Region> g;
    private final GText num;
    private final int buildingIndex;
    private final int level;

    public UpgradeButt(GETTER.GETTER_IMP<Region> g, GText num, int buildingIndex, int level){
        body.setDim(40, 40);
        this.g = g;
        this.num = num;
        this.buildingIndex = buildingIndex;
        this.level = level;
    }

    @Override
    protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
        GCOLOR.UI().border().render(r, body,-1);

        if (RD.UPGRADES().getLevel(g.get(), buildingIndex) == level) {
            COLOR.WHITE100.render(r, body,-2);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
        }else {
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
        }

        num.clear();
        num.color(COLOR.WHITE100);
        GFORMAT.toNumeral(num, level + 1);
        num.renderCY(r, body.x1() + 16 - (4*level), body.cY());

        if (level > 0) {
            if (!RD.BUILDINGS().all.get(buildingIndex).getBlue().upgrades().requires(level).passes(FACTIONS.player())) {
                OPACITY.O50.bind();
                COLOR.BLACK.render(r, body, -1);
                OPACITY.unbind();
            }
        }
    }

    @Override
    protected void clickA() {
        RD.UPGRADES().setLevel(g.get(), buildingIndex, level);
        VIEW.inters().popup.close();
    }
}
