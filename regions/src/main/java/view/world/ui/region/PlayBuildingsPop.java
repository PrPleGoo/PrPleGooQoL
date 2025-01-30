package view.world.ui.region;

import java.util.LinkedList;

import game.GAME;
import game.boosting.BHoverer;
import game.boosting.BOOSTABLES;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import init.C;
import init.resources.RESOURCE;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
import prplegoo.regions.api.MagicStringChecker;
import prplegoo.regions.ui.FoodSelector;
import prplegoo.regions.ui.SlaveSelector;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER.GETTER_IMP;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.panel.GPanel;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuildPoints.RDBuildPoint;
import world.region.building.RDBuilding;
import world.region.building.RDBuildingCat;
import world.region.building.RDBuildingLevel;

class PlayBuildingsPop {

    private final COLOR[] buCols = COLOR.interpolate(new ColorImp(100, 100, 100), new ColorImp(127, 110, 10), 16);
    private final GText num = new GText(UI.FONT().S, 8);
    public static int width = 64+16;
    public static final int height = 64+24;
    private final GETTER_IMP<Region> g;
    private Region current;
    private Levs levs = new Levs();

    private static CharSequence ¤¤RemoveAll = "Remove all constructed buildings?";
    private static CharSequence ¤¤Constructed = "This building has been constructed.";
    private static CharSequence ¤¤ConstructedUp = "This building can be upgrades.";
    private static CharSequence ¤¤Available = "This building can be constructed.";

    static {
        D.ts(PlayBuildingsPop.class);
    }

    private final GuiSection s = new GuiSection() {

        @Override
        public void render(SPRITE_RENDERER r, float ds) {
            if (g.get() != current || VIEW.current() != VIEW.world())
                VIEW.inters().section.close();
            GAME.SPEED.tmpPause();
            RD.BUILDINGS().tmp(false, g.get());
            super.render(r, ds);
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            RD.BUILDINGS().tmp(false, g.get());
            super.hoverInfoGet(text);
        };

    };

    public PlayBuildingsPop(PlayBuildings buildings, GETTER_IMP<Region> g){

        this.g = g;

        s.add(new Info());


        LinkedList<RENDEROBJ> rows = new LinkedList<>();
        int wam = 6;
        int hi = 0;

        for (RDBuildingCat cat : RD.BUILDINGS().cats) {
            int i = 0;
            GuiSection row = new GuiSection();
            rows.add(row);
            Butt[] butts = new Butt[wam];
            for (RDBuilding b : cat.all()) {
                if (i >= wam) {
                    butts = new Butt[wam];
                    row = new GuiSection();
                    rows.add(row);
                    i = 0;
                }
                Butt bb = new Butt(b);
                hi = bb.body.height()+12;
                butts[i] = bb;
                if (i == 0 || MagicStringChecker.isSlaverBuilding(b.key())) {
                    row.addRightC(0, bb);
                } else {
                    row.add(bb, butts[i - 1].body.x2(), butts[i - 1].body.y1());
                }

                if(MagicStringChecker.isFoodStallBuilding(b.key())){
                    GuiSection foodSelector = new FoodSelector(g);
                    row.addRightC(0, foodSelector);
                }

                if(MagicStringChecker.isSlaverBuilding(b.key())){
                    GuiSection slaveSelector = new SlaveSelector(g);
                    row.addRightC(0, slaveSelector);
                }
                i++;
            }
            rows.add(new RENDEROBJ.RenderDummy(1, 12));
        }

        int hh =  C.HEIGHT()-200-s.body().height();
        hh = (int) Math.ceil((double)hh/hi);
        hh *= hi;

        GScrollRows sc = new GScrollRows(rows, hh);
        s.addRelBody(8, DIR.N, sc.view());

        GPanel p = new GPanel();
        p.setBig();
        p.inner().set(s);
        p.setCloseAction(new ACTION() {

            @Override
            public void exe() {
                VIEW.inters().section.close();
            }

        });
        s.add(p);
        s.moveLastToBack();
        s.body().centerIn(C.DIM());


    }

    public void pop(RECTANGLE body) {
        if (VIEW.inters().section.current() == s)
            return;
        current = g.get();
        RD.BUILDINGS().tmp(true, g.get());
        if (body.x1() <= s.body().width())
            s.body().moveX1(body.x2());
        else {

            s.body().moveX2(body.x1());
            if (s.body().x2() > C.DIM().width())
                s.body().moveX2(C.DIM().width());
        }
        VIEW.inters().section.activate(s);
    }

    private class LevelButt extends ClickableAbs{

        private final RDBuilding bu;
        private final int level;

        LevelButt(RDBuilding b, int level){
            body.setDim(128, 40);
            this.bu = b;
            this.level = level;
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
            GCOLOR.UI().border().render(r, body,-1);

            if (RD.BUILDINGS().tmp().level(bu, g.get()) == level) {
                COLOR.WHITE100.render(r, body,-2);
                GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
            }else {
                GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
            }

            bu.levels().get(level).icon.big.renderCY(r, body().x1()+8, body().cY());
            num.clear();
            num.color(COLOR.WHITE100);
            GFORMAT.toNumeral(num, level);
            num.renderCY(r, body().x1()+48, body.cY());

            if (level > RD.BUILDINGS().tmp().level(bu, g.get())) {
                if (RD.BUILDINGS().tmp().canAfford(bu, g.get(), level) != null) {
                    OPACITY.O50.bind();
                    COLOR.BLACK.render(r, body, -1);
                    OPACITY.unbind();
                }
            }

        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {

            Region reg = g.get();
            GBox b = (GBox) text;
            b.title(bu.levels().get(level).name);
            if (level == 0)
                return;

            CharSequence prob = bu.canAfford(reg,  RD.BUILDINGS().tmp().level(bu, g.get()), level);
            if (prob != null)
                b.error(prob);
            b.NL();

            b.text(bu.info.desc);

            b.sep();
            for (int i = level; i > 0; i--) {
                if (bu.levels().get(i).reqs.hover(text, reg)) {
                    b.sep();
                    break;
                }
            }


            hoverCosts(reg, bu, RD.BUILDINGS().tmp().level(bu, g.get()), level, text);
            hoverNonCosts(reg, bu, RD.BUILDINGS().tmp().level(bu, g.get()), level, text);


        }

        @Override
        protected void clickA() {

            if (S.get().developer || RD.BUILDINGS().tmp().canAfford(bu, g.get(), level) == null) {

                RD.BUILDINGS().tmp().levelSet(bu, level);
//				bu.level.set(g.get(), level);
//				if (level < bu.level.get(g.get()))
//					bu.level.set(g.get(), level);
                VIEW.inters().popup.close();

            }


//			if (bu.level.isMax(reg))
//				bu.level.set(reg, 0);
//			else
//				bu.level.inc(reg, 1);
        }


    }

    private class RecipeButt extends ClickableAbs{

        private final Industry industry;
        private final RoomBlueprintImp blue;
        private final RDBuilding bu;
        private final int industryIndexOnBlue;

        RecipeButt(RDBuilding bu, Industry industry, RoomBlueprintImp blue, int industryIndexOnBlue){
            body.setDim(128, 40);
            this.industry = industry;
            this.blue = blue;
            this.bu = bu;
            this.industryIndexOnBlue = industryIndexOnBlue;
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
            GCOLOR.UI().border().render(r, body,-1);

            if (RD.RECIPES().isEnabled(g.get(), blue, industryIndexOnBlue)) {
                COLOR.WHITE100.render(r, body,-2);
                GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-4);
            }else {
                GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);
            }

            this.industry.outs().get(0).resource.icon().big.renderCY(r, body().x1()+8, body().cY());
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
                RD.RECIPES().setRecipe(g.get(), blue, industryIndexOnBlue);
                VIEW.inters().popup.close();
            }
        }
    }

    private void renderEfficiency(RDBuilding bu, RECTANGLE body, SPRITE_RENDERER r) {
        double d = bu.baseEfficiency(g.get())-1;
        d*= 2;
        int am = 0;
        if (d < 0) {
            am = (int) (-d*7);
        }else {
            am = (int) (d*4);
        }
        am = CLAMP.i(am, -7, 7);

        if (am != 0) {
            am = Math.abs(am);
            SPRITE s = UI.icons().s.chevron(DIR.N);
            if (d<0) {
                COLOR.RED100.bind();
                s = UI.icons().s.chevron(DIR.S);
            }else {
                COLOR.GREEN100.bind();
            }
            for (int i = 0; i < am; i++) {
                s.render(r, body.x2()-18, body.y1()+i*8);
            }

        }
        COLOR.unbind();
    }

    private static ArrayListResize<BoostSpec> tmp = new ArrayListResize<>(16);

    private void hoverNonCosts(Region reg, RDBuilding bu, int fromL, int toL, GUI_BOX text) {
        GBox b = (GBox) text;

        b.add(b.text().lablify().add(Dic.¤¤Effects));
        b.NL();

        tmp.clearSoft();
        for (BoostSpec s : bu.levels.get(fromL).local.all()) {
            if (RD.BUILDINGS().costs.get(s.boostable, s.booster) != null) {
                continue;
            }
            tmp.add(s);
        }

        for (BoostSpec s : bu.levels.get(toL).local.all()) {
            if (RD.BUILDINGS().costs.get(s.boostable, s.booster) != null) {
                continue;
            }
            boolean has = false;
            for (BoostSpec s2 : tmp) {
                if (s.isSameAs(s2) && Str.isSame(s.tName, s2.tName)) {
                    has = true;
                    break;
                }

            }
            if (!has)
                tmp.add(s);
        }


        for (BoostSpec s : tmp) {
            if (RD.BUILDINGS().costs.get(s.boostable, s.booster) != null) {
                continue;
            }
            bu.boosters().hover(b, s, getB(bu, fromL, toL, s, true), 0);
            b.NL();
        }
    }

    private void hoverRecipe(Industry industry, GUI_BOX text) {
        GBox b = (GBox) text;

        b.add(b.text().lablify().add(Dic.¤¤ProductionRate));
        b.NL();

        for (Industry.IndustryResource res : industry.outs()){
            hoverCost(b, res.resource.icon(), res.resource.name, res.rate);
        }

        for (Industry.IndustryResource res : industry.ins()){
            hoverCost(b, res.resource.icon(), res.resource.name, -res.rate);
        }
    }

    private static void hoverCosts(Region reg, RDBuilding bu, int fromL, int toL, GUI_BOX text) {
        GBox b = (GBox) text;

        b.NL(8);
        b.textLL(Dic.¤¤Cost);
        b.NL();

        int cr =  credits(bu, fromL, toL);
        if (cr > 0) {
            hoverCost(text, UI.icons().s.money, Dic.¤¤Curr, -cr, (int)FACTIONS.player().credits().getD());
            b.NL();
        }

        for (BoostSpec s : bu.boosters().all()) {
            if (RD.BUILDINGS().costs.get(s.boostable, s.booster) != null) {

                double value = getB(bu, fromL, toL, s, true);

                hoverCost(text, s.boostable.icon, s.boostable.name, value, s.boostable.get(reg));
                b.NL();
            }
        }
    }

    private static double getB(RDBuilding bu, int fromL, int toL, BoostSpec spec, boolean local) {
        double am = 0;
        RDBuildingLevel l = bu.levels.get(toL);
        boolean mm = spec.booster.isMul;
        for (BoostSpec boo : l.local.all()) {
            if (spec.isSameAs(boo) && Str.isSame(spec.tName, boo.tName)) {
                am += boo.booster.to();
                if (mm)
                    am -= 1;
            }
        }
        l = bu.levels.get(fromL);
        for (BoostSpec boo : l.local.all()) {

            if (spec.isSameAs(boo) && Str.isSame(spec.tName, boo.tName)) {
                am -= boo.booster.to();
                if (mm)
                    am += 1;
            }
        }
        if (mm)
            am += 1;
        return am;
    }

    private static void hoverCost(GUI_BOX text, SPRITE icon, CharSequence name, double value, double current) {

        if (value == 0)
            return;
        GBox b = (GBox) text;

        b.add(icon);
        GText nn = b.text();
        GText vv = b.text();
        nn.normalify2();
        vv.normalify2();
        nn.add(name);
        GFORMAT.iOrF(vv, value);
        if (value > 0) {
            nn.normalify();
            vv.normalify();
        }else if (current  < -value) {
            nn.errorify();
            vv.errorify();
        }else {
            nn.normalify2();
            vv.normalify2();
        }

        b.add(nn);
        b.tab(7);
        b.add(vv);
        b.tab(9);
        GText cc = b.text();
        cc.add('(');
        GFORMAT.iOrF(cc, current).add(')');
        b.add(cc);
        b.NL();
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

    private static class Levs extends RENDEROBJ.RenderImp {
        RDBuilding bu;
        int current;
        private final GText num = new GText(UI.FONT().S, 8);

        Levs(){
            super(Icon.L+4);
        }

        @Override
        public void render(SPRITE_RENDERER r, float ds) {
            for (int i = 1; i < bu.levels.size(); i++) {
                int x1 = body.x1()+(i-1)*(Icon.L+4);
                bu.levels.get(i).icon.big.render(r, x1, body.y1());

                num.clear();
                GFORMAT.toNumeral(num, i);
                num.adjustWidth();

                OPACITY.O75.bind();
                num.color(COLOR.BLACK);
                num.render(r, x1+1, body.y1()+1);
                num.color(GCOLOR.T().H1);
                OPACITY.unbind();
                num.render(r, x1, body.y1());
//

                if (current != i) {
                    OPACITY.O50.bind();
                    COLOR.BLACK.render(r,  x1, x1+Icon.L, body.y1(),  body.y1()+Icon.L);
                    OPACITY.unbind();
                }

            }
        }

        RENDEROBJ get(RDBuilding bu, int level) {
            this.bu = bu;
            this.current = level;
            body.setWidth(bu.levels.size()*Icon.L);
            return this;
        }

    }



    private static int credits(RDBuilding bu, int fromL, int toL) {
        int cost = bu.levels.get(toL).cost-bu.levels.get(fromL).cost;
        return cost;
    }



    public class Butt extends ClickableAbs{

        private final GuiSection lPop = new GuiSection() {
            @Override
            public final void render(SPRITE_RENDERER r, float ds) {
                if (VIEW.inters().section.current() != s)
                    VIEW.inters().popup2.close();
                super.render(r, ds);
            }
        };
        protected final RDBuilding bu;

        Butt(RDBuilding b){
            body.setDim(width, height);
            this.bu = b;
            for (int i = b.levels().size()-1; i >= 0; i--) {
                lPop.addDown(0, new LevelButt(b, i));
            }
            RoomBlueprintImp blue = b.getBlue();
            if(blue != null && blue instanceof INDUSTRY_HASER){
                INDUSTRY_HASER industryHaser = (INDUSTRY_HASER) blue;
                LIST<Industry> industries = industryHaser.industries();
                if(industries.size() > 1){
                    for (int i = industries.size()-1; i >= 0; i--) {
                        lPop.addDown(0, new RecipeButt(bu, industries.get(i), blue, i));
                    }
                }
            }
        }

        @Override
        protected void clickA() {

            if (RD.BUILDINGS().tmp().level(bu, g.get()) == 0) {
                if (S.get().developer || RD.BUILDINGS().tmp().canAfford(bu, g.get(), 1) == null) {
                    RD.BUILDINGS().tmp().levelSet(bu, 1);
                }
            }else {
                VIEW.inters().popup2.show(lPop, this);
            }


        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
            isSelected = RD.BUILDINGS().tmp().level(bu, g.get()) > 0;
            PlayBuildingsPop.this.render(bu, g.get(), body, r, isActive, isSelected, isHovered);

        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            PlayBuildingsPop.this.hover(bu, g.get(), text);
        }

    }

    public void render(RDBuilding bu, Region reg, RECTANGLE body, SPRITE_RENDERER r, boolean isActive, boolean isSelected, boolean isHovered) {

        ColorImp cc = ColorImp.TMP;
        cc.set(bu.cat.color);
        cc.render(r, body);
        cc.shadeSelf(0.5);
        cc.renderFrame(r, body, 0, 1);
        cc.renderFrame(r, body, -3, 1);

        int tl = RD.BUILDINGS().tmp().level(bu, g.get());

        Rec.TEMP.setDim(body.width()-4, body.height()-4);
        Rec.TEMP.moveC(body.cX(), body.cY());
        GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, Rec.TEMP);




        bu.levels().get(Math.max(tl, 1)).icon.huge.renderC(r, body.cX(), body.cY()+2);
        renderEfficiency(bu, body, r);



        if (tl > 0) {

            num.clear();
            GFORMAT.toNumeral(num, tl);
            num.adjustWidth();

            OPACITY.O75.bind();
            num.color(COLOR.BLACK);
            num.renderC(r, body.cX()+1, body.y1()+14+1);
            COLOR col = buCols[(int) ((double)(buCols.length-1)*RD.BUILDINGS().tmp().level(bu, g.get())/((bu.levels().size()-1)))];
            num.color(col);
            OPACITY.unbind();
            num.renderC(r, body.cX(), body.y1()+14);

            if ((tl <  bu.level.max(reg) && RD.BUILDINGS().tmp().canAfford(bu, g.get(), tl+1) == null)) {
                COLOR.YELLOW100.bind();
                UI.icons().s.chevron(DIR.N).renderC(r, body.cX()-8, body.y1()+4);
                UI.icons().s.chevron(DIR.N).renderC(r, body.cX(), body.y1()+4);
                UI.icons().s.chevron(DIR.N).renderC(r, body.cX()+8, body.y1()+4);
                COLOR.unbind();
            }

        }else {
            if (RD.BUILDINGS().tmp().canAfford(bu, g.get(), tl+1) != null) {
                OPACITY.O66.bind();
                COLOR.BLACK.render(r, body, -4);
                OPACITY.unbind();
            }

        }




        GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
    }

    public void hover(RDBuilding bu, Region reg, GUI_BOX text) {
        int lev = RD.BUILDINGS().tmp().level(bu, reg);
        GBox b = (GBox) text;
        if (lev == 0) {

            b.title(bu.info.name);
            CharSequence prob = RD.BUILDINGS().tmp().canAfford(bu, reg, lev+1);
            if (prob == null)
                b.add(b.text().normalify2().add(¤¤Available));
            else
                b.add(b.text().errorify().add(prob));
            b.NL(4);
            b.text(bu.info.desc);
            b.NL();
            b.add(levs.get(bu, 1));
            b.sep();

            BHoverer.hover(b, bu.baseFactors, reg, Dic.¤¤Efficiency, 1, false);
            b.sep();



            bu.levels().get(1).reqs.hover(text, reg);

            b.NL(8);
            hoverCosts(reg, bu, 0, 1, text);

            b.NL(8);
            hoverNonCosts(reg, bu, 0, 1, text);
        }else {

            RDBuildingLevel l = bu.levels().get(RD.BUILDINGS().tmp().level(bu, g.get()));
            b.title(l.name);
            CharSequence prob = RD.BUILDINGS().tmp().canAfford(bu, reg, lev+1);
            if (prob == null)
                b.add(b.text().normalify2().add(¤¤ConstructedUp));
            else
                b.add(b.text().normalify2().add(¤¤Constructed));
            b.NL(4);

            b.text(bu.info.desc);
            b.NL(2);
            b.add(levs.get(bu, lev));
            b.sep();

            if (bu.efficiency.all().size() > 0) {

                bu.efficiency.hover(b, reg, Dic.¤¤Efficiency, true);
                b.sep();


            }

            bu.boosters().hover(text, reg);
        }
    }

    private class Info extends GuiSection {


        Info() {

            CLICKABLE b;

            b = new GButt.ButtPanel(Dic.¤¤Accept) {
                @Override
                protected void clickA() {
                    if (g.get().faction() == FACTIONS.player())
                        FACTIONS.player().credits().inc(-RD.BUILDINGS().tmp().cost(), CTYPE.CONSTRUCTION);
                    RD.BUILDINGS().tmp().accept();
                    VIEW.inters().section.close();
                }

                @Override
                protected void renAction() {
                    if (!RD.BUILDINGS().tmp().hasChange()) {
                        activeSet(false);
                        return;
                    }

                    if (RD.BUILDINGS().tmp().canAfford()) {
                        activeSet(true);
                        return;
                    }

                    for (RDBuilding b : RD.BUILDINGS().all) {
                        if (RD.BUILDINGS().tmp().level(b, g.get()) > b.level.get(current)) {
                            activeSet(false);
                            return;
                        }
                    }


                    activeSet(true);
                }
            };
            addRightC(0, b);


            b = new GButt.ButtPanel(UI.icons().s.arrow_left) {

                @Override
                protected void clickA() {
                    RD.BUILDINGS().tmp(true, g.get());
                    super.clickA();
                }

                @Override
                protected void renAction() {
                    activeSet(RD.BUILDINGS().tmp().hasChange());
                };


            }.pad(4, 4).hoverInfoSet(Dic.¤¤cancel);
            addRightC(0, b);

            b = new GButt.ButtPanel(UI.icons().s.cancel) {

                @Override
                protected void clickA() {
                    for (RDBuilding b : RD.BUILDINGS().all) {
                        RD.BUILDINGS().tmp().levelSet(b, 0);
                    }
                    super.clickA();
                }

                @Override
                protected void renAction() {
                    boolean a = false;
                    for (RDBuilding b : RD.BUILDINGS().all)
                        if (RD.BUILDINGS().tmp().level(b, g.get()) > 0) {
                            a = true;
                            break;
                        }
                    activeSet(a);
                };

            }.pad(4, 4).hoverInfoSet(¤¤RemoveAll);
            addRightC(0, b);

            GuiSection butts = new GuiSection();

            butts.addRightC(16, new GStat() {

                @Override
                public void update(GText text) {
                    int am = RD.BUILDINGS().tmp().cost();
                    GFORMAT.i(text, -am);
                    if (am > FACTIONS.player().credits().getD())
                        text.errorify();
                    else if (am == 0)
                        text.color(COLOR.WHITE50);
                    else
                        text.normalify2();
                }
            }.hh(UI.icons().s.money));

            for (RDBuildPoint c : RD.BUILDINGS().costs.all) {
                butts.addRightC(64, boost(c.bo, c.icon));
            }

            butts.body().incrW(64);

            addRelBody(4, DIR.N, butts);




        }

        private RENDEROBJ boost(Boostable bo, SPRITE icon) {
            return new GStat() {

                @Override
                public void update(GText text) {
                    bo.get(g.get());
                    GFORMAT.iIncr(text, (int)bo.get(g.get()));
                }


                @Override
                public void hoverInfoGet(GBox b) {

                    b.title(bo.name);
                    b.text(bo.desc);
                    b.sep();
                    bo.hover(b, g.get(), null, true);
                };

            }.hh(icon);
        }

    }


}
