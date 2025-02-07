package view.world.ui.region;

import game.boosting.Boostable;
import game.boosting.Booster;
import game.time.TIME;
import init.resources.Growable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.text.D;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDOutput;
import world.region.RDOutputs.RDResource;
import prplegoo.regions.api.RDSlavery;

final class PlayOutput extends GuiSection{

    private static CharSequence ¤¤ship = "This resource is shipped annually after harvest. Harvest is in {0} days.";
    static {
        D.ts(PlayOutput.class);
    }
    private final ArrayListGrower<PlayButt> butts = new ArrayListGrower<PlayButt>();
    private final GETTER_IMP<Region> g;
    private final ArrayList<RENDEROBJ> activeButts;
    private final int width;
    public static final int height = 30;
    private final int amX;

    public PlayOutput(GETTER_IMP<Region> g, int width) {

        {
            INTE ii = new INTE() {

                @Override
                public int min() {
                    return 0;
                }

                @Override
                public int max() {
                    return RD.OUTPUT().taxRate.max(g.get());
                }

                @Override
                public int get() {
                    return RD.OUTPUT().taxRate.get(g.get());
                }

                @Override
                public void set(int t) {
                    RD.OUTPUT().taxRate.set(g.get(), t);
                }
            };

            GSliderInt sl = new GSliderInt(ii, 140, 24, true) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    RD.OUTPUT().taxRate.info().hover(text);
                    super.hoverInfoGet(text);
                }
            };
            sl.body().moveX2(body().x2()-16);
            sl.body().moveY1(4);
            add(sl);
        }



        this.width = (width-32)/5;
        this.g = g;
        for (int i = 0; i < RD.OUTPUT().ALL.size(); i++) {
            butts.add(new ResButt(RD.OUTPUT().ALL.get(i)));
        }

        for (int i = 0; i < RD.RACES().all.size(); i++) {
            butts.add(new RaceButt(RD.SLAVERY().get(i)));
        }

        activeButts = new ArrayList<>(butts.size());

        {
            amX = 5;
            GTableBuilder builder = new GTableBuilder() {

                @Override
                public int nrOFEntries() {
                    return (int) Math.ceil(activeButts.size()/(double)amX);
                }
            };

            builder.column(null, amX*this.width, new GRowBuilder() {

                @Override
                public RENDEROBJ build(GETTER<Integer> ier) {
                    return new Row(ier);
                }
            });

            addRelBody(2, DIR.S, builder.createHeight((height)*3, false));
        }

        pad(6, 6);
    }

    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        GButt.ButtPanel.renderBG(r, true, false, false, body());
        GButt.ButtPanel.renderFrame(r, body());
        activeButts.clearSloppy();
        for (PlayButt b : butts) {
            if (hasValue(b.getBoostable(), g.get())) {
                activeButts.add(b);
            }
        }

        super.render(r, ds);
    }

    private boolean hasValue(Boostable bo, Region reg) {
        return bo.get(reg) != 0;
    }

    private class Row extends GuiSection{

        private final GETTER<Integer> ier;

        Row(GETTER<Integer> ier){
            this.ier = ier;
            body().setHeight(height);
        }

        @Override
        public void render(SPRITE_RENDERER r, float ds) {
            int x1 = body().x1();
            int y1 = body().y1();
            clear();
            int s = ier.get()*amX;
            for (int i = 0; i < amX && i+s < activeButts.size(); i++) {
                addRightC(0, activeButts.get(i+s));
            }
            body().moveX1(x1);
            body().moveY1(y1);
            super.render(r, ds);
        }

    }

    private abstract class PlayButt extends ClickableAbs {
        abstract Boostable getBoostable();
    }

    private class ResButt extends PlayButt{

        private final RDOutput bu;
        private final GText tt = new GText(UI.FONT().S, 8);

        ResButt(RDOutput b){
            body.setDim(width, height);
            this.bu = b;
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

            GCOLOR.UI().border().render(r, body);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-1);

            bu.boost.icon.medium.renderC(r, body.x1()+16, body.cY());

            tt.clear();
            GFORMAT.i(tt, (long) bu.boost.get(g.get()));

            tt.renderC(r, body.x1()+32, body.cY());
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            if (bu instanceof RDResource) {
                RDResource r = (RDResource) bu;
                Growable g = RESOURCES.growable().get(r.res);
                GBox b = (GBox) text;
                if (g != null) {
                    GText t = b.text();
                    t.add(¤¤ship);
                    int d = (int) (g.seasonalOffset*TIME.years().bitConversion(TIME.days()));
                    int now = TIME.days().bitsSinceStart()%(int)TIME.years().bitConversion(TIME.days());
                    if (now >= d) {
                        t.insert(0, now-d);
                    }else {
                        t.insert(0, d-now);
                    }
                    b.add(t);
                    b.sep();
                }
            }


            bu.boost.hover(text, g.get(), true);

        }

        @Override
        Boostable getBoostable() {
            return bu.boost;
        }
    }
    private class RaceButt extends PlayButt {
        private final RDSlavery.RDSlave rdSlave;
        private final GText tt = new GText(UI.FONT().S, 8);

        RaceButt(RDSlavery.RDSlave rdSlave) {
            body.setDim(width, height);
            this.rdSlave = rdSlave;
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

            GCOLOR.UI().border().render(r, body);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body, -1);

            rdSlave.rdRace.race.appearance().icon.renderC(r, body.x1() + 16, body.cY());

            tt.clear();
            GFORMAT.f(tt, rdSlave.boost.get(g.get()));

            tt.renderC(r, body.x1() + 32, body.cY());
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            rdSlave.boost.hover(text, g.get(), true);
        }

        @Override
        Boostable getBoostable() {
            return rdSlave.boost;
        }
    }
}
