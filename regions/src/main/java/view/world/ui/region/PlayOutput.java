package view.world.ui.region;

import game.boosting.Boostable;
import init.race.Race;
import init.resources.RESOURCE;
import init.sprite.UI.UI;
import init.trade.TR;
import prplegoo.regions.api.region.rd.RDSlavery;
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
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs;
import world.region.RDOutputs.RDOutput;
import world.region.RDOutputs.RDResource;
import world.region.updating.Shipper;

final class PlayOutput extends GuiSection{

    private static CharSequence ¤¤ship = "This resource is shipped annually in {0} days. Accumulated so far: {1}.";
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
            GButt b = new GButt.ButtPanel(RDOutputs.¤¤Squeeze) {

                @Override
                protected void clickA() {
                    RD.OUTPUT().squeze(g.get());
                    super.clickA();
                }

                @Override
                protected void renAction() {
                    activeSet(RD.RACES().loyaltyAll.getD(g.get()) > 0.40);
                }

                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GBox b = (GBox) text;
                    b.title(RDOutputs.¤¤Squeeze);
                    b.text(RDOutputs.¤¤SqueezeD);
                    b.NL();

                    b.add(UI.icons().s.money);
                    b.textLL(Dic.¤¤Currs);
                    b.tab(6);
                    b.add(GFORMAT.iIncr(b.text(), (long) (RD.OUTPUT().MONEY.boost.get(g.get())*RD.OUTPUT().sqeezeAmountDays)));
                    b.NL();

                    Shipper shipper = RD.UPDATER().getShipper();
                    extracted(shipper, b, g);

                    b.textLL(RD.RACES().loyaltyAll.info().name);
                    b.tab(6);
                    b.add(GFORMAT.perc(b.text(), -0.4));
                    b.NL();
                    super.hoverInfoGet(text);
                }

            };

            b.body().moveX2(body().x2()-16);
            b.body().moveY1(4);
            add(b);
        }

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

                    extracted(shipper, b, g);
                }
            };

            addRight(4, taxInfo);
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

    private static void extracted(Shipper shipper, GBox b, GETTER_IMP<Region> g) {
        for (RDResource res : RD.OUTPUT().RES) {
            if (!TR.RES().contains(res.res.index())) {
                continue;
            }

            int amount = shipper.getAccumulatedTaxes(g.get(), res);

            if (amount <= 0)
            {
                continue;
            }

            RESOURCE rr = TR.RES().get(res.res.index()).t;

            b.add(rr.icon());
            b.text(rr.name);

            b.tab(7);
            b.add(GFORMAT.i(b.text(), amount));

            b.NL();
        }

        for (int i = 0; i < RD.SLAVERY().all().size(); i++) {
            RDSlavery.RDSlave rdSlave = RD.SLAVERY().all().get(i);
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

    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        GButt.ButtPanel.renderBG(r, true, false, false, body());
        GButt.ButtPanel.renderFrame(r, body());
        activeButts.clearSloppy();
		for (PlayButt b : butts) {
			if (hasValue(b.getBoostables(), g.get())) {
                activeButts.add(b);
            }
        }
        super.render(r, ds);
    }

    private boolean hasValue(Boostable[] bos, Region reg) {
        for(Boostable bo : bos) {
            if (bo.get(reg) != 0) {
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
        abstract Boostable[] getBoostables();
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

            double am = bu.boost.get(g.get()) + bu.boostYearlyPart.get(g.get());

            if (bu instanceof RDResource && TR.RES().contains(((RDResource) bu).res.index())) {
                RDResource res = (RDResource) bu;
                am -= RD.INPUTS().get(TR.RES().get(res.res.index()).t).get(g.get());

                if (am > 0) {
                    am -= Math.min(am, RD.LOGISTICS().get(TR.RES().get(res.res.index()).t).getDelivery(g.get()));
                }
            }

            tt.clear();
            GFORMAT.i(tt, (long) Math.floor(am));

            tt.renderC(r, body.x1()+32, body.cY());



        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            GBox b = (GBox) text;

            if (bu.boostYearlyPart.get(g.get()) > 0) {
                bu.boostYearlyPart.hover(text, g.get(), true);
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

                b.sep();

                if (bu instanceof RDResource && TR.RES().contains(((RDResource) bu).res.index())) {
                    RDResource res = (RDResource) bu;

                    RD.INPUTS().get(TR.RES().get(res.res.index()).t).hover(text, g.get(), true);
                    b.sep();
                    RD.LOGISTICS().get(TR.RES().get(res.res.index()).t).boost.hover(text, g.get(), true);
                }
            }
        }

        @Override
        Boostable[] getBoostables() {
            if (bu instanceof RDResource && TR.RES().contains(((RDResource) bu).res.index())) {
                RDResource res = (RDResource) bu;

                return new Boostable[]{bu.boost, RD.INPUTS().get(TR.RES().get(res.res.index()).t)};
            }
            return new Boostable[] { bu.boost };
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
        Boostable[] getBoostables() {
            return new Boostable[] { rdSlave.boost };
        }
    }

}
