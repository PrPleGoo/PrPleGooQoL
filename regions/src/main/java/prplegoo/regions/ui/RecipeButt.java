package prplegoo.regions.ui;

import init.resources.RESOURCE;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
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

public class RecipeButt extends CLICKABLE.ClickableAbs {
    private final GETTER.GETTER_IMP<Region> g;
    private final GText num;
    private final Industry industry;
    private final RoomBlueprintImp blue;
    private final RDBuilding bu;
    private final int industryIndexOnBlue;

    public RecipeButt(GETTER.GETTER_IMP<Region> g, GText num, RDBuilding bu, Industry industry, RoomBlueprintImp blue, int industryIndexOnBlue){
        body.setDim(128, 40);
        this.g = g;
        this.num = num;
        this.industry = industry;
        this.blue = blue;
        this.bu = bu;
        this.industryIndexOnBlue = industryIndexOnBlue;
    }

    @Override
    protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
        GCOLOR.UI().border().render(r, body,-1);

        if (RD.RECIPES().isEnabled(g.get(), bu.index(), blue, industryIndexOnBlue)) {
            COLOR.WHITE100.render(r, body,-2);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
        }else {
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
        }

        int rendered = 0;
        for (IndustryResource res : industry.ins()){
            res.resource.icon().medium.renderCY(r, body().x1()+4+(24*rendered), body().cY());
            rendered++;
        }

        UI.icons().m.arrow_right.medium.renderCY(r, body().x1()+4+(24*rendered), body().cY());
        rendered++;

        for (IndustryResource res : industry.outs()){
            res.resource.icon().medium.renderCY(r, body().x1()+4+(24*rendered), body().cY());
            rendered++;
        }

        num.clear();
        num.color(COLOR.WHITE100);
        num.renderCY(r, body().x1()+48, body.cY());

        if (!this.industry.lockable().passes(g.get().faction())) {
            OPACITY.O50.bind();
            COLOR.BLACK.render(r, body, -1);
            OPACITY.unbind();
        }

    }

    @Override
    public void hoverInfoGet(GUI_BOX text) {
        GBox b = (GBox) text;

        b.title(bu.info.name);
        b.sep();

        hoverRecipe(industry, text);
    }

    @Override
    protected void clickA() {
        if (S.get().developer || this.industry.lockable().passes(g.get().faction())) {
            RD.RECIPES().setRecipe(g.get(), bu.index() ,blue, industryIndexOnBlue);
            VIEW.inters().popup.close();
        }
    }

    public static void hoverRecipe(Industry industry, GUI_BOX text) {
        GBox b = (GBox) text;

        b.add(b.text().lablify().add(Dic.¤¤ProductionRate));
        b.NL();

        for (IndustryResource res : industry.outs()){
            hoverCost(b, res.resource.icon(), res.resource.name, res.rate);
        }

        for (IndustryResource res : industry.ins()){
            hoverCost(b, res.resource.icon(), res.resource.name, -res.rate);
        }
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
