package prplegoo.regions.ui;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

public class OptionalConsumptionButt extends CLICKABLE.ClickableAbs {
    private final GETTER.GETTER_IMP<Region> g;
    private final GText num;
    private final int buildingIndex;
    private final int resourceIndex;

    public OptionalConsumptionButt(GETTER.GETTER_IMP<Region> g, GText num, int buildingIndex, int resource){
        body.setDim(128, 40);
        this.g = g;
        this.num = num;
        this.buildingIndex = buildingIndex;
        this.resourceIndex = resource;
    }

    @Override
    protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
        GCOLOR.UI().border().render(r, body,-1);

        if (RD.OPTIONAL_CONSUMPTION().isEnabled(g.get(), buildingIndex, resourceIndex)) {
            COLOR.WHITE100.render(r, body,-2);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
        }else {
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
        }

        RESOURCES.ALL().get(resourceIndex).icon().medium.renderCY(r, body().x1()+4, body().cY());

        num.clear();
        num.color(COLOR.WHITE100);
        num.renderCY(r, body().x1()+48, body.cY());
    }

    @Override
    public void hoverInfoGet(GUI_BOX text) {
        GBox b = (GBox) text;

        RDBuilding building = RD.BUILDINGS().all.get(buildingIndex);

        b.title(building.info.name);
        b.sep();

        hoverOptionalConsumption(resourceIndex, text);
    }

    @Override
    protected void clickA() {
        RD.OPTIONAL_CONSUMPTION().flip(g.get(), buildingIndex, resourceIndex);
        VIEW.inters().popup.close();
    }

    public void hoverOptionalConsumption(int resourceIndex, GUI_BOX text) {
        GBox b = (GBox) text;

        b.add(b.text().lablify().add(Dic.¤¤ConsumptionRate));
        b.NL();

        RESOURCE resource = RESOURCES.ALL().get(resourceIndex);

        double rate = RD.OPTIONAL_CONSUMPTION().getRate(buildingIndex, resourceIndex);
        hoverCost(b, resource.icon(), resource.name, rate);
    }

    private static void hoverCost(GUI_BOX text, SPRITE icon, CharSequence name, double value) {

        if (value == 0)
            return;
        GBox b = (GBox) text;

        b.add(icon);
        GText nn = b.text();
        GText vv = b.text();
        nn.add(name);
        GFORMAT.iOrF(vv, value);
        if (value > 0) {
            nn.normalify2();
            vv.normalify2();
        } else {
            nn.errorify();
            vv.errorify();
        }

        b.add(nn);
        b.tab(7);
        b.add(vv);
        b.NL();
    }
}
