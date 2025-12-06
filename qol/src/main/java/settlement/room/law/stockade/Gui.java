package settlement.room.law.stockade;

import game.GAME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.sprite.UI.UI;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GChart;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import view.ui.message.MessageSection;

class Gui extends UIRoomModuleImp<StockInstance, ROOM_STOCKADE> {

    private static CharSequence ¤¤Food = "¤Food To Fetch";
    private static CharSequence ¤¤setAll = "¤Sentence all captives to be: {0}.";
    private static CharSequence ¤¤setSure = "¤Are you sure you wish to inflict the punishment: {0} on all captives?";

    private static CharSequence ¤¤mWTitle = "Security low";
    private static CharSequence ¤¤mWBody = "Since our stockades are poorly staffed or poorly supplied with food, we are running the risk of incidents occurring.";
    private static CharSequence ¤¤mTitle = "Prisoner Escape!";
    private static CharSequence ¤¤mBody = "Since our stockade was poorly staffed and tended, the prisoners have escaped!";
    private static CharSequence ¤¤emp = "¤Insufficient employees or lack of food might cause incidents. Full employment is required.";
    private static CharSequence ¤¤cancel = "Cancel all manually assigned punishments, and let the prisoners be punished according to your law settings.";

    private static boolean[] raceFilter;

    static {
        D.ts(Gui.class);
    }

    Gui(ROOM_STOCKADE s) {
        super(s);

        raceFilter = new boolean[RACES.all().size()];
        for (Race r : RACES.all()) {
            raceFilter[r.index] = true;
        }
    }

    @Override
    protected void appendPanel(GuiSection section, GGrid grid, GETTER<StockInstance> g, int x1, int y1) {

        GuiSection s = new GuiSection();
        int i = 0;

        for (ResG e : RESOURCES.EDI().all()) {

            GButt.ButtPanel b = new GButt.ButtPanel(e.resource.icon()) {

                @Override
                protected void renAction() {
                    selectedSet(g.get().fetch.has(e.resource));
                }

                @Override
                protected void clickA() {
                    g.get().fetch.toggle(e.resource);
                    g.get().jobs.resNotFound.clear();
                }

                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    GBox b = (GBox) text;
                    b.title(e.resource.names);
                    b.textLL(Dic.¤¤Consumed).add(GFORMAT.i(b.text(), (int) -blueprint.indu.ins().get(e.index()).year.get(g.get())));
                }

            };
            b.pad(4, 4);

            s.add(b, (i%4)*b.body().width(), (i/4)*b.body().height());
            i++;



        }
        s.addRelBody(8, DIR.N, new GHeader(¤¤Food));

        s.addRelBody(8, DIR.S, new GStat() {

            @Override
            public void update(GText text) {
                GFORMAT.iofk(text, g.get().prisonersCurrent, g.get().prisonersMax);
            }
        }.hh(Dic.¤¤Capacity));

        section.addRelBody(8, DIR.S, s);

        {
            GuiSection ss = new GuiSection();

            int gi = 0;

            {
                GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.cancel) {

                    @Override
                    protected void clickA() {
                        makePrisoners(g.get());
                        for (Humanoid h : list) {
                            if(!raceFilter[h.race().index]){
                                continue;
                            }

                            if (AIModule_Prisoner.DATA().punishmentSet.get(h.ai()) != null) {
                                AIModule_Prisoner.DATA().punishmentSet.set(h.ai(), null);
                                h.interrupt();
                            }

                        }
                    }

                    @Override
                    protected void renAction() {
                        boolean a = false;
                        makePrisoners(g.get());
                        for (Humanoid h : list) {
                            if (AIModule_Prisoner.DATA().punishmentSet.get(h.ai()) != null) {
                                if(!raceFilter[h.race().index]){
                                    continue;
                                }

                                a = true;
                                break;
                            }

                        }
                        activeSet(a);
                    }

                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        GBox b = (GBox) text;
                        b.text(¤¤cancel);
                    }

                };

                b.setDim(40, 40);
                ss.addGrid(b, gi++, 6, 2, 2);
            }

            for (Punishment p : LAW.process().punishments) {
                GButt.ButtPanel b = new GButt.ButtPanel(p.icon) {

                    ACTION a = new ACTION() {

                        @Override
                        public void exe() {
                            makePrisoners(g.get());
                            for (Humanoid h : list) {
                                if(!raceFilter[h.race().index]){
                                    continue;
                                }

                                AIModule_Prisoner.DATA().punishmentSet.set(h.ai(), p);
                                h.interrupt();
                            }
                        }
                    };

                    @Override
                    protected void clickA() {
                        if (KEYS.MAIN().MOD.isPressed()) {
                            a.exe();
                        } else {
                            VIEW.inters().yesNo.activate(Str.TMP.clear().add(¤¤setSure).insert(0, p.action), a, ACTION.NOP, true);
                        }
                    }

                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        GBox b = (GBox) text;
                        GText t = b.text();
                        t.add(¤¤setAll).insert(0, p.action);
                        b.add(t);
                    }

                };

                b.setDim(40, 40);

                ss.addGrid(b, gi++, 6, 2, 2);
            }

            section.addRelBody(8, DIR.S, ss);
        }

        {
            GuiSection ss = new GuiSection();
            for (Race r : RACES.all()) {
                GButt.ButtPanel b = new GButt.ButtPanel(r.appearance().icon) {
                    @Override
                    protected void clickA() {
                        raceFilter[r.index] = !raceFilter[r.index];
                        selectedSet(raceFilter[r.index]);
                    }

                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        GBox b = (GBox) text;
                        GText t = b.text();
                        t.add(r.info.name);
                        b.add(t);
                    }

                };

                b.selectedSet(raceFilter[r.index]);

                b.setDim(40, 40);

                ss.addGrid(b, r.index(), 6, 2, 2);
            }

            section.addRelBody(8, DIR.S, ss);
        }

        GTableBuilder b = new GTableBuilder() {

            @Override
            public int nrOFEntries() {
                makePrisoners(g.get());
                return list.size();
            }
        };

        b.column(null, 280, new GRowBuilder() {


            @Override
            public RENDEROBJ build(GETTER<Integer> ier) {
                return new CLICKABLE.ClickableAbs(280, 54) {



                    @Override
                    public void hoverInfoGet(GUI_BOX text) {
                        int k = ier.get();
                        if (k >= list.size())
                            return;
                        Humanoid h = list.get(k);
                        h.hover((GBox) text);

                    }

                    @Override
                    protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
                                          boolean isHovered) {
                        GCOLOR.UI().border().render(r, body,-1);
                        GCOLOR.UI().bg(isActive, isSelected, isHovered).render(r, body,-2);

                        int k = ier.get();
                        if (k >= list.size())
                            return;
                        Humanoid h = list.get(k);
                        int x1 = body().x1();
                        STATS.APPEARANCE().portraitRender(r, h.indu(), body().x1(), body().y1(), 1);

                        Str t = Str.TMP;

                        t.clear();
                        t.add(STATS.LAW().prisonerType.get(h.indu()).name);
                        GCOLOR.T().H1.bind();
                        UI.FONT().M.render(r, t, x1+50, body().y1()+8);

                        t.clear();
                        t.add(AIModule_Prisoner.punishment(h, h.ai()).action);
                        GCOLOR.T().H2.bind();
                        UI.FONT().S.render(r, t, x1+50, body().y1()+32);

                    }

                    @Override
                    protected void clickA() {
                        int k = ier.get();
                        if (k >= list.size())
                            return;
                        Humanoid h = list.get(k);
                        h.click();
                    }
                };
            }
        });
        int he = ISidePanel.HEIGHT-section.body().height()-16;
        section.addRelBody(8, DIR.S, b.createHeight(he, false));

    }

    @Override
    protected void hover(GBox b, StockInstance ins) {
        b.NL();
        b.textLL(Dic.¤¤Capacity);
        b.tab(6);
        b.add(GFORMAT.iofk(b.text(), ins.prisonersCurrent, ins.prisonersMax));

        b.NL();
    }

    @Override
    protected void problem(StockInstance i, Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings) {

        if (i.riotChance < 1 && (i.employees().employed() < i.employees().max() || !i.jobs.resNotFound.isClear())) {

            errors.add(¤¤emp);
        }

        super.problem(i, free, errors, warnings);
    }

    @Override
    protected void appendMain(GGrid gg, GGrid text, GuiSection sExtra) {
        GuiSection s = new GuiSection();
        GChart cc = new GChart();
        int i = 0;
        for (ResG e : RESOURCES.EDI().all()) {
            RENDEROBJ r = new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.i(text, -blueprint.indu.ins().get(e.index()).history().get());
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    b.title(e.resource.name);
                    b.textLL(Dic.¤¤Consumed).add(GFORMAT.i(b.text(), (int) -blueprint.indu.ins().get(e.index()).history().get()));
                    b.NL();
                    cc.clear();
                    cc.add(blueprint.indu.ins().get(e.index()).history());
                    b.add(cc);


                };
            }.hv(e.resource.icon());

            s.add(r, (i%4)*42, (i/4)*48);
            i++;

        }

        s.addRelBody(8, DIR.S, new GStat() {

            @Override
            public void update(GText text) {
                GFORMAT.iofk(text, blueprint.prisoners, blueprint.prisonersMax);
            }
        }.hh(Dic.¤¤Capacity));

        text.add(s);


    }

    private final ArrayListResize<Humanoid> list = new ArrayListResize<>(164, 1024*2);
    private int upI = -1;

    private void makePrisoners(StockInstance ins) {
        if (upI == GAME.updateI())
            return;
        list.clearSoft();
        if (ins == null)
            return;
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (e instanceof Humanoid) {
                Humanoid a = (Humanoid) e;
                if (AIModule_Prisoner.isPrisoner(a, ins)) {
                    list.add(a);
                }
            }
        }
        upI = GAME.updateI();
    }

    static void mWarn(StockInstance ins) {
        new Mess(¤¤mWTitle, ¤¤mWBody, ins.body().cX(), ins.body().cY()).send();

    }

    static void m(StockInstance ins) {

        new Mess(¤¤mTitle, ¤¤mBody, ins.body().cX(), ins.body().cY()).send();

    }


    private static class Mess extends MessageSection {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int tx,ty;
        private String desc;

        Mess(CharSequence title, CharSequence desc, int tx, int ty){
            super(title);
            this.desc = ""+desc;
            this.tx = tx;
            this.ty = ty;
        }

        @Override
        protected void make(GuiSection section) {

            paragraph(desc);
            section.addRelBody(16, DIR.N, SETT.ROOMS().STOCKADE.iconBig().scaled(2));
            section.addRelBody(16, DIR.S, new GButt.ButtPanel(UI.icons().m.crossair) {
                @Override
                protected void clickA() {
                    VIEW.s().activate();
                    VIEW.s().getWindow().centererTile.set(tx, ty);
                }
            });

        }

    }

}
