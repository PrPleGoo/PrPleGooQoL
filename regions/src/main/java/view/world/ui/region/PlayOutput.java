package view.world.ui.region;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutput.RDResource;

final class PlayOutput extends GuiSection{



    private final RENDEROBJ[] butts = new RENDEROBJ[RD.OUTPUT().all.size()];
    private final GETTER_IMP<Region> g;
    private final ArrayList<RENDEROBJ> activeButts = new ArrayList<RENDEROBJ>(RD.OUTPUT().all.size());
    private final int width;
    public static final int height = 30;
    private final int amX;

    public PlayOutput(GETTER_IMP<Region> g, int width) {
        this.width = (width-24)/5;
        this.g = g;
        for (int i = 0; i < RD.OUTPUT().all.size(); i++) {
            butts[i] = new ResButt(RD.OUTPUT().all.get(i));
        }

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

        add(builder.createHeight((height)*2, false));


    }

    @Override
    public void render(SPRITE_RENDERER r, float ds) {
        activeButts.clearSloppy();
        for (int i = 0; i < RD.OUTPUT().all.size(); i++) {
            RDResource b = RD.OUTPUT().all.get(i);
            if (b.boost.get(g.get()) > 0) {
                activeButts.add(butts[i]);
            }
        }
        super.render(r, ds);
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

    private class ResButt extends ClickableAbs{

        private final RDResource bu;
        private final GText tt = new GText(UI.FONT().S, 8);

        ResButt(RDResource b){
            body.setDim(width, height);
            this.bu = b;
        }

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

            GCOLOR.UI().border().render(r, body);
            GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-1);

            bu.res.icon().renderC(r, body.x1()+16, body.cY());

            tt.clear();
            GFORMAT.i(tt, (long) bu.boost.get(g.get()));

            tt.renderC(r, body.x1()+32, body.cY());



        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            bu.boost.hover(text, g.get(), true);

        }

    }

}
