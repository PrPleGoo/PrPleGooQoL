package view.world.ui.region;

import game.boosting.Boostable;
import game.boosting.Booster;
import game.time.TIME;
import init.race.Race;
import init.resources.Growable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.room.industry.module.IndustryResource;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
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
import util.text.D;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDOutput;
import world.region.RDOutputs.RDResource;
import prplegoo.regions.api.RDSlavery;
import world.region.updating.Shipper;

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
            GButt.ButtPanel taxInfo = new GButt.ButtPanel(UI.icons().s.time) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GBox b = (GBox) text;

                    Shipper shipper = RD.UPDATER().getShipper();

                    int daysUntilTaxes = shipper.daysUntilTaxes(g.get());

                    if (daysUntilTaxes < 0) {
                        b.text("Taxes are shipped annually");

                        return;
                    }

                    b.text("Taxes are shipped annually in " + daysUntilTaxes + " days.");
                    b.sep();
                    b.text(" Accumulated so far:");
                    b.NL();

                    for (RDResource res : RD.OUTPUT().RES) {
                        int amount = shipper.getAccumulatedTaxes(g.get(), res);

                        if (amount <= 0)
                        {
                            continue;
                        }

                        RESOURCE rr = res.res;

                        b.add(rr.icon());
                        b.text(rr.name);

                        b.tab(7);
                        b.add(GFORMAT.i(b.text(), amount));

                        b.NL();
                    }

                    for (RDSlavery.RDSlave rdSlave : RD.SLAVERY().all()) {
                        int amount = shipper.getAccumulatedTaxes(g.get(), rdSlave);

                        if (amount <= 0)
                        {
                            continue;
                        }

                        Race race = rdSlave.rdRace.race;

                        b.add(race.appearance().icon);
                        b.textL(race.info.name);
                        b.textL("(Prisoners)");

                        b.tab(7);
                        b.add(GFORMAT.i(b.text(), amount));

                        b.NL();
                    }
                }
            };

            addRight(4, taxInfo);
        }

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

            addRight(4, sl);

            sl.body().moveY1(sl.body().y1() - 2);
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

			addRelBody(2, DIR.S, builder.createHeight((height)*2, false));
        }

        pad(6, 6);
    }

    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        GButt.ButtPanel.renderBG(r, true, false, false, body());
        GButt.ButtPanel.renderFrame(r, body());
        activeButts.clearSloppy();
		for (PlayButt b : butts) {

			if (hasValue(b.getBoostable(), g.get())
                    || (b.hasYearlyBoostable() && hasValue(b.getYearlyBoostable(), g.get()))
            ) {
                activeButts.add(b);
            }
        }
        super.render(r, ds);
    }

    private boolean hasValue(Boostable bo, Region reg) {

		for (Booster b : bo.all()) {
			double v = b.get(reg);
			if ((!b.isMul && v != 0)) {
				return true;
			}
		}
		return false;
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
        abstract Boostable getYearlyBoostable();
        abstract boolean hasYearlyBoostable();
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
			GFORMAT.i(tt, (long) (bu.boost.get(g.get()) + bu.boostYearlyPart.get(g.get())));

            tt.renderC(r, body.x1()+32, body.cY());



        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {





			if (bu.boostYearlyPart.get(g.get()) > 0) {
				bu.boostYearlyPart.hover(text, g.get(), true);
                GBox b = (GBox) text;
				b.sep();
                    GText t = b.text();
                    t.add(¤¤ship);
				t.insert(0, bu.daysUntilDailydelivery());
				t.insert(1, bu.yearlyAccumilation.get(g.get()));
                    b.add(t);
                    b.sep();

				if (bu.boost.get(g.get()) > 0) {
                    bu.boost.hover(text, g.get(), true);
                }


            }else {
                bu.boost.hover(text, g.get(), true);
            }
        }

        @Override
        Boostable getBoostable() {
            return bu.boost;
        }
        @Override
        Boostable getYearlyBoostable() {
            return bu.boostYearlyPart;
        }
        @Override
        boolean hasYearlyBoostable() {
            return true;
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
        @Override
        Boostable getYearlyBoostable() {
            return rdSlave.boost;
        }
        @Override
        boolean hasYearlyBoostable() {
            return false;
        }
    }
}
