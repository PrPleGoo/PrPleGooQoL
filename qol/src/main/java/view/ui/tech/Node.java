package view.ui.tech;

import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.text.D;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;

final class Node extends GuiSection{

    public final static int WIDTH = 112;
    public final static int HEIGHT = 112;
    public static final COLOR Cdormant = COLOR.WHITE100.shade(0.3);
    public static final COLOR CUnlockable = COLOR.WHITE100.shade(0.5);
    public static final COLOR Chovered = COLOR.WHITE100.shade(0.8);
    public static final COLOR Callocated = new ColorImp(10, 35, 55);
    public static final COLOR Cfinished = new ColorImp(10, 60, 60);

    private static CharSequence ¤¤Relock = "¤Hold {0} and click to disable this technology. {1} Knowledge will be added to your frozen pool.";

    int level;

    static {
        D.ts(Node.class);
    }

    public final TECH tech;
    Node(TECH tech){
        this.tech = tech;
        body().setDim(WIDTH, HEIGHT);
        addC(new Content(tech), body().cX(), body().cY());

    }

    private class Content extends ClickableAbs{




        Content(TECH tech){
            body.setDim(88, 88);
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

            isHovered |= VIEW.UI().tech.tree.hoverededTechs[tech.index()];
            for (BoostSpec b : tech.boosters.all()) {
                if (b.boostable == VIEW.UI().tech.tree.hoveredBoost)
                    isHovered = true;
            }

            isSelected |= FACTIONS.player().tech.level(tech) > 0;

            GCOLOR.T().H1.render(r, body);

            GCOLOR.UI().bg(isActive, false, isHovered).render(r, body,-1);
            COLOR col = col(isHovered);
            col.render(r, body,-4);

            GCOLOR.UI().bg(isActive, false, isHovered).render(r, body,-7);

            {
                double levels = tech.levelMax;
                int level = FACTIONS.player().tech.level(tech);
                double d = level/levels;
                int y2 = body().y2()-8;
                int y1 = (int) (y2 - d*(body().height()-16));
                (d == 1.0 ? Cfinished : Callocated).render(r, body().x1()+8, body().x2()-8, y1, y2);

            }

            tech.icon().renderC(r, body);
            if (!isSelected) {
                if (FACTIONS.player().tech.costOfNextWithRequired(tech) > FACTIONS.player().tech().available().get() || !FACTIONS.player().tech.getLockable(tech).passes(FACTIONS.player())) {
                    OPACITY.O50.bind();
                    COLOR.BLACK.render(r, body, -1);
                    OPACITY.unbind();
                }
            }



            if (VIEW.UI().tech.tree.filteredTechs[tech.index()]) {
                OPACITY.O50.bind();
                COLOR.BLACK.render(r, body);
                OPACITY.unbind();
            }

            if (hoveredIs()) {

            }else {

            }




        }

        private COLOR col(boolean hovered) {
            if (hovered)
                return Chovered;
            return Cdormant;
//			if (FACTIONS.player().tech.level(tech) >= tech.levelMax)
//				return Cunlocked;
//			if  (FACTIONS.player().tech.costOfNextWithRequired(tech) <= FACTIONS.player().tech().available().get())
//				return Cunlockable;
//			return Cdormant;
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            GBox b = (GBox) text;
            text.title(tech.info.name);

            PTech t = FACTIONS.player().tech();

            {
                if (tech.levelMax == 1) {
                    if (t.level(tech) == 1) {
                        b.add(b.text().normalify2().add(Dic.¤¤Activated));
                    }
                }else {
                    b.textLL(Dic.¤¤Level);
                    b.add(GFORMAT.iofkNoColor(b.text(), t.level(tech), tech.levelMax));
                }

                b.tab(5);
                b.textLL(Dic.¤¤Allocated);
                b.add(SPRITES.icons().s.vial);
                b.add(GFORMAT.iBig(b.text(), t.costTotal(tech)));
                b.NL();



                if (t.level(tech) < tech.levelMax) {
                    b.NL(4);
                    b.textLL(Dic.¤¤Cost);
                    b.add(SPRITES.icons().s.vial);
                    int c = t.costLevelNext(tech);

                    if (t.available().get() < c)
                        b.add(GFORMAT.iBig(b.text(), c).errorify());
                    else
                        b.add(GFORMAT.iBig(b.text(), c));


                    b.tab(5);
                    b.textLL(Dic.¤¤TotalCost);
                    b.add(SPRITES.icons().s.vial);
                    int ct = t.costOfNextWithRequired(tech);
                    if (t.available().get() < ct)
                        b.add(GFORMAT.iBig(b.text(), ct).errorify());
                    else
                        b.add(GFORMAT.iBig(b.text(), ct));
                    b.NL(4);



                }


            }
            b.sep();
            {
                LIST<TechRequirement> rr = tech.requires();

                int am = 0;
                for (TechRequirement r : rr)
                    if (r.level > 0)
                        am++;

                FACTIONS.player().tech.getLockable(tech).hover(text, FACTIONS.player());

                if (am > 0) {
                    if (FACTIONS.player().tech.getLockable(tech).all().size() == 0)
                        b.textLL(Dic.¤¤Requires);
                    b.NL();
                    for (TechRequirement r : rr) {
                        if (r.level <= 0)
                            continue;
                        b.add(UI.icons().s.vial);
                        GText te = b.text();
                        te.add(r.tech.tree.name);
                        te.add(':').s();
                        te.add(r.tech.info.name);
                        if (r.tech.levelMax > 1) {
                            te.s().add(r.level);
                        }
                        if (t.level(r.tech) >= r.level)
                            te.normalify2();
                        else
                            te.errorify();
                        b.add(te);
                        b.NL();

                    }
                }
            }
            b.NL(8);

            tech.lockers.hover(text);

            b.NL(8);

            if (tech.boosters.all().size() > 0)

                if (tech.boosters.all().size() > 0) {
                    b.textLL(Dic.¤¤Effects);
                    b.tab(6);
                    b.textLL(Dic.¤¤Current);
                    b.tab(9);
                    b.textLL(Dic.¤¤Next);
                    b.NL();

                    for (BoostSpec bb : tech.boosters.all()) {
                        b.add(bb.boostable.icon);
                        b.text(bb.boostable.name);
                        b.tab(6);
                        double v = bb.booster.to();
                        if (bb.booster.isMul)
                            v -= 1;
                        v*=t.level(tech);
                        if (bb.booster.isMul)
                            v += 1;
                        b.add(bb.booster.format(b.text(), v));

                        if (t.level(tech) < tech.levelMax) {
                            v = bb.booster.to();
                            if (bb.booster.isMul)
                                v -= 1;
                            v*=t.level(tech)+1;
                            if (bb.booster.isMul)
                                v += 1;

                            b.tab(9);
                            b.add(bb.booster.format(b.text(), v));
                        }



                        b.NL();
                    }
                    b.NL(8);
                }

            b.sep();

            text.text(tech.info.desc);
            b.NL();

            if (t.level(tech) > 0) {
                GText te = b.text();
                te.add(¤¤Relock);
                te.insert(0, "shift");
                te.insert(1, t.costLevel(tech, t.level(tech)));
                b.error(te);
            }


        }

        @Override
        protected void clickA() {
            if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_SHIFT) || CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_RIGHT_SHIFT))
                VIEW.UI().tech.tree.prompt.forget(tech);
            else
                VIEW.UI().tech.tree.prompt.unlock(tech);
            super.clickA();
        }

    }

}
