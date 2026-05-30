package view.world.ui.region;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.tmp.TmpBoostingButt;
import game.faction.FACTIONS;
import game.faction.FWorth;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINION;
import game.raiding.RaidingMap;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GHeader.HeaderVertical;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.map.regions.Region;
import world.map.regions.RegionInfo;
import world.region.RD;
import world.region.building.RDBuildPoints.RDBuildPoint;

final class PlayInfo extends GuiSection {

    private static CharSequence ¤¤abandon = "Abandon Region";
    private static CharSequence ¤¤abandonQ = "Do you wish to abandon this region? It will be turned over into the hands of rebels.";

    private static CharSequence ¤¤autonomy = "Give Autonomy";
    private static CharSequence ¤¤autonomyD = "Do you wish to give this region autonomy and bestow upon it self-rule? The new faction will be in your debt.";
    private static CharSequence ¤¤autonomyE = "There simply are no suitable faction rulers to appoint a new king here. Perhaps we can gift this region to an ally or vassal instead?";

    static {
        D.ts(PlayInfo.class);
    }

    PlayInfo(GETTER_IMP<Region> g, int WIDTH) {

        int i = 0;
        int cols = 7;
        int width = 78;
        int height = 48;
        DIR align = DIR.C;


        {
            SPRITE s = new SPRITE.Imp(48, 16) {

                @Override
                public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

                    double c = get(RD.HEALTH().getD(g.get()));
                    double t = get(RD.HEALTH().boostablee.get(g.get()));

                    GMeter.renderC(r, c, t, X1, X2, Y1, Y2);

                    if (RD.HEALTH().outbreak.get(g.get()) == 1) {
                        Y1-= 24;
                        OPACITY.O25TO100.bind();
                        UI.icons().m.disease.render(r, X1, Y1);
                        OPACITY.unbind();
                    }


                }

                private double get(double d) {
                    return CLAMP.d(d, 0, 1);
                }
            };

            GHeader.HeaderVertical h = new HeaderVertical(UI.icons().m.heart, s) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GBox b = (GBox) text;
                    RD.HEALTH().hover(b, g.get());
                }
            };

            addGridD(h, i++, cols, width, height, align);
        }

        {
            RENDEROBJ h = new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.perc(text, CLAMP.d(RD.DIST().bProximity.get(g.get()), 0, 1));
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    b.title(RD.DIST().bProximity.name);
                    b.text(RD.DIST().bProximity.desc);
                    b.sep();
                    RD.DIST().bProximity.hover(b, g.get(), null, true);
                };

            }.hv(UI.icons().m.wheel);

            addGridD(h, i++, cols, width, height, align);
        }

        {
            SPRITE s = new SPRITE.Imp(48, 16) {

                @Override
                public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                    GMeter.render(r, GMeter.C_REDGREEN, RD.OWNER().affiliation.getD(g.get()), X1,X2,Y1,Y2);
                }
            };

            GHeader.HeaderVertical h = new HeaderVertical(UI.icons().m.flag, s) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GBox b = (GBox) text;
                    b.textLL(Dic.¤¤Current);
                    b.tab(6);
                    b.add(GFORMAT.perc(b.text(), RD.OWNER().affiliation.getD(g.get())));
                    b.NL(8);
                    b.add(RD.OWNER().affiliation.info());
                }
            };

            addGridD(h, i++, cols, width, height, align);
        }

        {
            RENDEROBJ o = new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.percInv(text, RD.DEVASTATION().current.getD(g.get()));
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    b.add(RD.DEVASTATION().current.info());
                };

            }.hv(UI.icons().m.skull);
            addGridD(o, i++, cols, width, height, align);
        }

        {
            RENDEROBJ o = new GStat() {

                @Override
                public void update(GText text) {
                    double v = CLAMP.d(GAME.raiders().entry.get(g.get()).probability(), -100, 100);
                    GFORMAT.f0Inv(text, v, 0);
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    b.title(RaidingMap.¤¤Name);
                    b.text(RaidingMap.¤¤desc);
                    b.NL();

                    b.add(UI.icons().s.money);
                    b.textLL(Dic.¤¤Riches);
                    b.tab(6);
                    b.add(GFORMAT.i(b.text(), (int)FWorth.region(g.get())));
                    b.NL();

                    b.add(UI.icons().s.shield);
                    b.textLL(Dic.¤¤Garrison);
                    b.tab(6);
                    b.add(GFORMAT.i(b.text(), (int)RD.MILITARY().power.getD(g.get())));
                    b.NL();

                    b.add(UI.icons().s.sword);
                    b.textLL(Dic.¤¤Armies);
                    b.tab(6);
                    b.add(GFORMAT.i(b.text(), (int)GAME.raiders().entry.get(g.get()).army()));
                    b.NL();

                    b.sep();

                    double v = CLAMP.d(GAME.raiders().entry.get(g.get()).probability(), -100, 100);

                    b.textLL(Dic.¤¤Current);
                    b.tab(6);
                    b.add(GFORMAT.f0Inv(b.text(), v, 0));
                };

            }.hv(UI.icons().m.raider);
            addGridD(o, i++, cols, width, height, align);
        }

        for (RDBuildPoint c : RD.BUILDINGS().costs.ALL){
            RENDEROBJ o = new GStat() {

                @Override
                public void update(GText text) {
                    double value = c.bo == BOOSTABLES.CIVICS().GOV && g.get().faction() != null
                        ? c.bo.get(g.get().faction())
                        : c.bo.get(g.get());
                    GFORMAT.iIncr(text, (int)value);
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    BOOSTABLE_O boostableValue = c.bo == BOOSTABLES.CIVICS().GOV && g.get().faction() != null
                            ? g.get().faction()
                            : g.get();
                    c.hover(b, boostableValue);



                };

            }.hv(c.icon);
            addGridD(o, i++, cols, width, height, align);
        }




        addRelBody(4, DIR.N, actions(g, width));
        pad((WIDTH-body().width())/2, 0);


    }

    private static GuiSection actions(GETTER_IMP<Region> g, int WIDTH) {
        GuiSection butts = new GuiSection();

        butts.addRightC(0, new GButt.ButtPanel(UI.icons().m.noble) {

            ACTION aa = new ACTION() {

                @Override
                public void exe() {

                    RD.setFaction(g.get(), null, true);
                    FactionNPC f = FACTIONS.activateNext(g.get(), null, true);
                    f.generate(RD.RACES().get(FACTIONS.player().race()), true);
                    ROPINION.OTHER().liberate(f);
                    DIP.VASSAL().set(f, FACTIONS.player());
                    GAME.events().world.dip.dismissWelcome(f);

                }
            };

            @Override
            protected void clickA() {
                if (FACTIONS.frees() > 5)
                    VIEW.inters().yesNo.activate(¤¤autonomyD, aa, ACTION.NOP, true);
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                text.title(¤¤autonomy);
                if (FACTIONS.frees() <= 5)
                    ((GBox)text).error(¤¤autonomyE);
            }

        });

        butts.addRightC(0, new GButt.ButtPanel(UI.icons().m.cancel) {

            ACTION aa = new ACTION() {

                @Override
                public void exe() {
                    RD.setFaction(g.get(), null, true);
                }
            };

            @Override
            protected void clickA() {
                VIEW.inters().yesNo.activate(¤¤abandonQ, aa, ACTION.NOP, true);
            }

        }.hoverTitleSet(¤¤abandon));


        GuiSection pop = MiscBasics.info(g);
        StringInputSprite name = new StringInputSprite(RegionInfo.nameSize, UI.FONT().H2) {
            @Override
            protected void change() {
                g.get().info.name().clear().add(text());
            };
        };
        pop.add(new GInput(name), 0, -48);

        butts.addRightC(0, new GButt.ButtPanel(UI.icons().m.questionmark) {

            @Override
            protected void clickA() {
                name.text().clear().add(g.get().info.name());
                VIEW.inters().popup.show(pop, this);
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                text.title(Dic.¤¤Info);
            }

        });

        butts.addRightC(0, TmpBoostingButt.make(g, GAME.BOOST().regions));

        if (S.get().developer) {

            butts.addRightC(0, new GButt.ButtPanel(UI.icons().m.cog) {

                PlayDebug dd = new PlayDebug();

                @Override
                protected void clickA() {
                    dd.reg = g.get();
                    VIEW.inters().popup.show(dd, this);
                }

            });

        }

        return butts;
    }



}
