package view.world.ui.region;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.raiding.RaidingMap;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDOutput;

final class ListPlayer extends ISidePanel{


    ListPlayer(ISidePanels panels) {


        GTableBuilder bu = new GTableBuilder() {

            @Override
            public int nrOFEntries() {
                return FACTIONS.player().realm().regions()-1;
            }

            @Override
            public void hoverInfo(int index, GBox box) {
                Region reg = FACTIONS.player().realm().region(index+1);
                VIEW.world().UI.regions.hover(reg, box);
            }

            @Override
            public void click(int index) {
                Region reg = FACTIONS.player().realm().region(index+1);
                VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
                ISidePanel p = VIEW.world().UI.regions.get(reg);
                panels.add(ListPlayer.this, true);
                panels.add(p, false);
                super.click(index);
            }

            @Override
            public boolean selectedIs(int index) {
                Region reg = FACTIONS.player().realm().region(index+1);
                return VIEW.world().UI.regions.active(reg);
            }

        };

        HOVERABLE title;

        bu.column(Dic.¤¤name, 120, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat(UI.FONT().S) {

                    @Override
                    public void update(GText text) {
                        text.setMaxWidth(110);
                        text.setMultipleLines(false);
                        text.lablify().add(reg(ier).info.name());
                    }
                }.r(DIR.NW);
            }
        });

        title = new HOVERABLE.Sprite(UI.icons().s.human).hoverTitleSet(Dic.¤¤Population);
        bu.column(title, 140, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat() {

                    @Override
                    public void update(GText text) {
                        int popCurrent = RD.RACES().population.get(reg(ier));
                        GFORMAT.i(text, popCurrent);

                        text.add(" / ");

                        int popTarget = (int) RD.RACES().popTarget.getValue(reg(ier));
                        GFORMAT.i(text, popTarget);

                    }
                }.r(DIR.E);
            }
        }, DIR.E);

        title = new HOVERABLE.Sprite(UI.icons().s.happy).hoverTitleSet(RD.RACES().loyaltyAll.info().name);
        bu.column(title, 48, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, RD.RACES().loyaltyAll.getD(reg(ier)));
                    }
                }.r(DIR.E);
            }
        }, DIR.E);

        title = new HOVERABLE.Sprite(UI.icons().s.heart).hoverTitleSet(RD.HEALTH().name);
        bu.column(title, 48, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, RD.HEALTH().getD(reg(ier)));
                    }
                }.r(DIR.E);
            }
        }, DIR.E);

        title = new HOVERABLE.Sprite(UI.icons().s.shield).hoverTitleSet(RaidingMap.¤¤Name);
        bu.column(title, 48, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, CLAMP.d(GAME.raiders().entry.get(reg(ier)).security(), -1, 1));
                    }

                    @Override
                    public void hoverInfoGet(GBox b) {

                    };

                }.r(DIR.E);
            }
        }, DIR.E);

        title = new HOVERABLE.Sprite(UI.icons().s.hammer).hoverTitleSet(RD.SLAVERY().getWorkforce().info.name);
        bu.column(title, 48, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new GStat() {
                    @Override
                    public void update(GText text) {
                        GFORMAT.i(text, (int) RD.SLAVERY().getWorkforce().bo.get(reg(ier)));
                    }
                }.r(DIR.E);
            }
        }, DIR.E);

        title = new HOVERABLE.Sprite(UI.icons().s.arrow_left).hoverTitleSet(RTYPE.TAX.name);
        bu.column(title, Icon.S*10, new GRowBuilder() {

            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {

                return new RENDEROBJ.RenderImp(120, Icon.L) {

                    @Override
                    public void render(SPRITE_RENDERER r, float ds) {
                        int i = 0;
                        int dy = 0;
                        for (RDOutput res : RD.OUTPUT().ALL) {
                            if (res.getDelivery(reg(ier)) > 0) {
                                res.boost.icon.render(r, body.x1()+i*Icon.S, body.y1()+dy*Icon.S*dy);
                                i+= dy;
                                dy += 1;
                                dy &= 1;
                                if (i >= 10)
                                    break;
                            }
                        }
                    }
                };
            }
        }, DIR.E);


        section.add(bu.createHeight(HEIGHT-64, true));

        section.addDownC(8, new GButt.ButtPanel(Dic.¤¤All) {

            @Override
            protected void clickA() {
                VIEW.world().UI.regions.openOtherList();
            }

        });




        titleSet(Dic.¤¤Realm);
    }

    private static Region reg(GETTER<Integer> ier) {
        return FACTIONS.player().realm().region(ier.get()+1);
    }





}
