package view.world.ui.region;

import init.sprite.UI.UI;
import init.text.D;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.dic.Dic;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;

class PlayBuildings extends GuiSection{


    private final ArrayList<RENDEROBJ> activeButts = new ArrayList<RENDEROBJ>(RD.BUILDINGS().all.size());
    private final RENDEROBJ[] butts = new RENDEROBJ[RD.BUILDINGS().all.size()+1];

    private static CharSequence ¤¤Click = "Click to construct buildings.";

    static {
        D.ts(PlayBuildings.class);
    }

    public int width = 64+8;
    public static final int height = 64+24;
    private final int amX;

    private final PlayBuildingsPop build;

    PlayBuildings(GETTER_IMP<Region> g, int width, int height){

        build = new PlayBuildingsPop(null, g);
        this.width = ((width-7*4)/7)&~0b01;
        for (int i = 0; i < RD.BUILDINGS().sorted.size(); i++) {
            final RDBuilding bu = RD.BUILDINGS().sorted.get(i);

            butts[i] = new HOVERABLE.HoverableAbs(PlayBuildingsPop.width, PlayBuildingsPop.height) {

                @Override
                protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
                    build.render(bu, g.get(), body, r, true, false, isHovered);
                }

                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    build.hover(bu, g.get(), text);
                }
            };

        }

        amX = (width-24)/(this.width);




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


        int hi = height-body().height()-16;
        int h = hi/(PlayBuildings.height+16);
        RENDEROBJ cc = h < 1 ? builder.createHeight(PlayBuildings.height, false) :  builder.createHeight((PlayBuildings.height+16)*h, false);

        GuiSection sec = new GuiSection() {

            @Override
            public void render(SPRITE_RENDERER r, float ds) {
                boolean hov = hoveredIs();
                GButt.ButtPanel.renderBG(r, true, false, hov, body());
                GButt.ButtPanel.renderFrame(r, body());

                activeButts.clearSloppy();
                for (int i = 0; i <RD.BUILDINGS().sorted.size(); i++) {
                    RDBuilding b = RD.BUILDINGS().sorted.get(i);
                    if (RD.BUILDINGS().tmp().level(b, g.get()) != 0) {
                        activeButts.add(butts[i]);
                    }
                }

                if (activeButts.size() == 0) {
                    UI.icons().m.building.renderC(r, body());
                }

                super.render(r, ds);
            }

            @Override
            public boolean click() {
                build.pop(PlayBuildings.this.body());
                return true;
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                super.hoverInfoGet(text);
                if (text.emptyIs()) {
                    text.title(Dic.¤¤Buildings);
                    text.text(¤¤Click);
                }
            }

        };
        sec.add(cc);
        sec.pad(6);
        addRelBody(12, DIR.S, sec);


//		if (h < 1)
//			addRelBody(12, DIR.S, builder.createHeight(PlayBuildings.height, false));
//		else
//			addRelBody(12, DIR.S, builder.createHeight((PlayBuildings.height+16)*h, false));
//
//
//

    }

    private class Row extends GuiSection{

        private final GETTER<Integer> ier;

        Row(GETTER<Integer> ier){
            this.ier = ier;
            body().setHeight(height+16);
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
            body().setHeight(height+16);
            body().moveX1(x1);
            body().moveY1(y1);
            super.render(r, ds);
        }

    }



}
