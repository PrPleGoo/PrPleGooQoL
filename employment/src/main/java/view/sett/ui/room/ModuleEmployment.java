package view.sett.ui.room;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import settlement.room.main.job.ROOM_EMPLOY_AUTO;
import settlement.stats.STATS;
import settlement.stats.muls.StatsMultipliers.StatMultiplierAction;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GChart;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.slider.GTarget;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleEmployment implements ModuleMaker {

    private final CharSequence ¤¤EMPLOYEES_DESC = "¤Actual employees / Target Employees. When you set the target, unemployed people will soon sign up and start working the room. Without sufficient workers, or if subjects are prevented from working due to priorities, a room will perform poorly. The worker amount isn't fixed. It can vary greatly depending on how the room is laid out, and how well your city is planned. The tool-tip when creating a room will offer a guess of many workers will be required in an average city.";
    private final CharSequence ¤¤WORKLOAD_LOW = "¤Workload is low";
    private final CharSequence ¤¤SHIFT_START = "¤What time the work shift starts.";
    private final CharSequence ¤¤SHIFT_NIGHT = "¤This room is employed all hours of the day.";
    private final CharSequence ¤¤WORKERS_INC = "¤Workers +{0}";
    private final CharSequence ¤¤WORKERS_DEC = "¤Workers -{0}";
    private final CharSequence ¤¤WORKERS_NONE = "¤Insufficient workers available. If there are idle subjects, these will soon sign up and start working.";
    private final CharSequence ¤¤WORKERS_INSUFFICIENT = "¤Insufficient workers allocated.";
    private final CharSequence ¤¤WORKERS_INSPECT = "¤Inspect";
    private final CharSequence ¤¤AUTO = "¤Auto Employ";
    private final CharSequence ¤¤AUTO_DESC = "¤Let the AI adjust worker amount each day based on workload.";

    ModuleEmployment(Init init){
        D.t(this);
    }


    @Override
    public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
        if (p instanceof RoomBlueprintIns<?>) {
            RoomBlueprintIns<?> pi = (RoomBlueprintIns<?>) p;
            if (pi.employment() != null) {
                l.add(new I(pi));

            }
        }

    }

    // This is the x/y employment count in the main window
    private class I extends UIRoomModule {

        private final RoomBlueprintIns<?> blueprint;
        private final GChart chart = new GChart();

        I(RoomBlueprintIns<?> blue){
            this.blueprint = blue;
        }

        @Override
        public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {
            grid.add(new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.iofkInv(text,
                            blueprint.employment().employed(),
                            blueprint.employment().neededWorkers());
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    // None of this info is job specific
                    if (blueprint.employmentExtra() != null) {
                        b.title(STATS.WORK().EMPLOYED.info.name);
                        b.text(¤¤EMPLOYEES_DESC);
                        b.NL(8);
                        b.add(chart.sprite(blueprint.employmentExtra().history()));
                    }

                };

            }.hh(SPRITES.icons().s.human));



            if (blueprint.employmentExtra() != null) {

                grid.add(new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, blueprint.employment().efficiency());

                    }
                }.hh(SPRITES.icons().s.cog).hoverInfoSet(RoomEmploymentIns.¤¤WorkloadD));

                grid.add(new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, blueprint.employment().proximity());

                    }
                }.hh(SPRITES.icons().s.wheel).hoverInfoSet(RoomEmploymentIns.¤¤ProximityD));
            }




            if (blueprint.employment().worksNights()) {
                grid.add(new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.f(text, Double.NaN);
                    }
                }.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤SHIFT_NIGHT));
            }else {
                grid.add(new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.i(text, (int)(blueprint.employment().getShiftStart()*TIME.hoursPerDay()));
                    }
                }.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤SHIFT_START));
            }

            if (blueprint.employmentExtra() != null){


                GTarget t = new GTarget(20, false, true, blueprint.employmentExtra().priority);
                RENDEROBJ r = new CLICKABLE.Pair(new GHeader(SPRITES.icons().s.alert), t, DIR.E, 2).hoverInfoSet(Dic.¤¤Priority);
                grid.add(r);
            }

            grid.add(new GStat() {

                @Override
                public void update(GText text) {
                    int emp = blueprint.employment().employed();
                    if (emp == 0)
                        return;
                    GFORMAT.perc(text, emp*blueprint.employment().accidentsPerYear / (BOOSTABLES.CIVICS().ACCIDENT.get(POP_CL.clP())), 2);
                    text.normalify();

                }
            }.hh(SPRITES.icons().s.death).hoverTitleSet(Dic.¤¤AccidentRate).hoverInfoSet(Dic.¤¤AccidentRateD));





        }

        @Override
        public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
                                       LISTE<UIRoomBulkApplier> appliers) {

            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+25)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()+25);
                }
            });
            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+5)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()+5);
                }
            });
            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_INC).insert(0, ""+1)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()+1);
                }
            });
            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+1)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()-1);
                }
            });
            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+5)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()-5);
                }
            });
            appliers.add(new UIRoomBulkApplier(new Str(¤¤WORKERS_DEC).insert(0, ""+25)) {

                @Override
                protected void apply(RoomInstance t) {
                    t.employees().neededSet(t.employees().needed()-25);
                }
            });

            if (blueprint instanceof ROOM_EMPLOY_AUTO) {
                appliers.add(new UIRoomBulkApplier(new Str(¤¤AUTO).s().add(Dic.¤¤on)) {

                    @Override
                    protected void apply(RoomInstance t) {

                        boolean b = ((ROOM_EMPLOY_AUTO)t.blueprint()).autoEmploy(t);
                        if (!b && t.employees().needed() == 0)
                            (t).employees().neededSet(1);

                        ((ROOM_EMPLOY_AUTO)t.blueprint()).autoEmploy(t, true);
                    }
                });
                appliers.add(new UIRoomBulkApplier(new Str(¤¤AUTO).s().add(Dic.¤¤off)) {

                    @Override
                    protected void apply(RoomInstance t) {


                        ((ROOM_EMPLOY_AUTO)t.blueprint()).autoEmploy(t, false);
                    }
                });
            }


            if (STATS.MULTIPLIERS().OVERTIME.canMark(blueprint)) {
                appliers.add(new UIRoomBulkApplier(STATS.MULTIPLIERS().OVERTIME.name) {

                    @Override
                    protected void apply(RoomInstance t) {
                        for (Humanoid a : t.employees().employees()) {
                            if (STATS.MULTIPLIERS().OVERTIME.canBeMarked(a.indu()))
                                STATS.MULTIPLIERS().OVERTIME.mark(a, true);
                        }

                    }
                });
                appliers.add(new UIRoomBulkApplier(STATS.MULTIPLIERS().DAY_OFF.name) {

                    @Override
                    protected void apply(RoomInstance t) {
                        for (Humanoid a : t.employees().employees()) {
                            if (STATS.MULTIPLIERS().DAY_OFF.canBeMarked(a.indu()))
                                STATS.MULTIPLIERS().DAY_OFF.mark(a, true);
                        }

                    }
                });
            }



            if (blueprint.employmentExtra() != null) {
                sorts.add(new GTSort<RoomInstance>(RoomEmploymentIns.¤¤Workload) {

                    @Override
                    public int cmp(RoomInstance current, RoomInstance cmp) {
                        double e1 = current.employees().efficiency();
                        double e2 = cmp.employees().efficiency();
                        if (e1 == e2)
                            return 0;
                        if (e1 < e2)
                            return -1;
                        return 1;
                    }

                    @Override
                    public void format(RoomInstance h, GText text) {
                        GFORMAT.perc(text, h.employees().efficiency());
                    }

                });

            }

        }

        @Override
        public void appendButt(GuiSection s, GETTER<RoomInstance> get) {

            INTE in = new INTE() {

                @Override
                public int min() {
                    return 0;
                }

                @Override
                public int max() {
                    return get.get().employees().max();
                }

                @Override
                public int get() {
                    return get.get().employees().needed();
                }

                @Override
                public void set(int t) {
                    get.get().employees().neededSet(t);
                }
            };

            RENDEROBJ oo = new RENDEROBJ.RenderImp(20, 18) {

                GStat st = new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.i(text, Math.min(get.get().employees().employed(), get.get().employees().needed()));
                    }
                };

                @Override
                public void render(SPRITE_RENDERER r, float ds) {
                    int e = get.get().employees().employed();
                    int n = get.get().employees().needed();
                    e = Math.min(e, n);
                    if (n > 0)
                        GMeter.render(r, GMeter.C_REDGREEN, (double)e/n, body.x1()-20, body.x2()+20, body.y1(), body.y2());
                    OPACITY.O50.bind();
                    COLOR.BLACK.render(r, body.cX()-st.width()/2-2, body.cX()+st.width()/2+2, body.y1(), body.y2());
                    OPACITY.unbind();
                    st.renderC(r, body);

                }
            };



            GTarget ss = new GTarget(52, false, true, oo, in) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    text.title(Dic.¤¤Employees);
                    GBox b = (GBox) text;
                    b.add(GFORMAT.iofkInv(b.text(), get.get().employees().employed(), get.get().employees().needed()));
                }
            };

            s.addRelBody(8, DIR.E, ss);

            s.addRightC(32, new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.perc(text, get.get().employees().efficiency());
                }

                @Override
                public void hoverInfoGet(GBox b) {
                    b.title(RoomEmploymentIns.¤¤Workload);
                    b.add(GFORMAT.perc(b.text(), get.get().employees().efficiency()));
                };

            }.decrease().r(DIR.NE));

            if (blueprint instanceof ROOM_EMPLOY_AUTO) {
                CLICKABLE c = new GButt.Checkbox() {

                    @Override
                    protected void clickA() {
                        boolean b = ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get());
                        if (!b && ((RoomInstance)get.get()).employees().needed() == 0)
                            ((RoomInstance)get.get()).employees().neededSet(1);

                        ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get(), !b);
                    }
                    @Override
                    protected void renAction() {
                        selectedSet(((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get()));
                    }

                }.hoverTitleSet(¤¤AUTO).hoverInfoSet(¤¤AUTO_DESC);
                c.body().moveCY(s.getLast().cY());
                c.body().moveX2(s.body().width());
                s.addRightC(8, c);

            }

        }

        @Override
        public void hover(GBox box, Room room, int rx, int ry) {
            RoomInstance i = (RoomInstance) room;
            box.text(i.blueprint().employment().title);
            box.add(GFORMAT.iofkInv(box.text(), i.employees().employed(), i.employees().needed()));
            box.space();
            box.text();
            if (i.blueprintI().employmentExtra() != null)
                box.text(RoomEmploymentIns.¤¤Workload).add(GFORMAT.perc(box.text(), i.employees().efficiency()));
            box.NL();
            highlightWorkers(room);
        }

        @Override
        public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room room, int rx,
                            int ry) {
            RoomInstance i = (RoomInstance) room;

            if (i.blueprintI().employmentExtra() != null) {

                if(i.employees().employed() < i.employees().needed()) {
                    if(i.employees().employed() == 0) {
                        errors.add(¤¤WORKERS_NONE);
                    }else
                        warnings.add(¤¤WORKERS_INSUFFICIENT);
                }
                if ((1.0-i.employees().efficiency())*(i.employees().employed()-1) > 1){
                    warnings.add(¤¤WORKLOAD_LOW);
                }

            }



            if (room instanceof JOBMANAGER_HASER) {
                JOBMANAGER_HASER h = (JOBMANAGER_HASER) room;
                boolean m = false;
                for (RESOURCE r : RESOURCES.ALL()) {
                    if (!h.getWork().resourceReachable(r)) {
                        m = true;
                    }

                }
                if (m) {
                    Str s = free.pop();
                    s.add(Dic.¤¤Unavailable);
                    for (RESOURCE r : RESOURCES.ALL()) {
                        if (!h.getWork().resourceReachable(r)) {
                            s.s().add(r.name).add(',');


                        }
                    }
                    errors.add(s);

                }
            }
        }

        private void highlightWorkers(Room room) {
            for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
                if (e instanceof Humanoid) {
                    Humanoid a = (Humanoid) e;
                    if (STATS.WORK().EMPLOYED.get(a) == room) {
                        SETT.OVERLAY().add(e);
                    }
                }
            }
        }

        @Override
        public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {

            GuiSection s = new GuiSection() {
                @Override
                public void render(SPRITE_RENDERER r, float ds) {

                    highlightWorkers(get.get());
                    super.render(r, ds);
                    GCOLOR.UI().border().render(r, section.body().x1()+8, section.body().x2()-8, body().y2()-1, body().y2());
                }
            };
            RENDEROBJ r;
            r = new GStat() {

                @Override
                public void update(GText text) {
                    GFORMAT.iofkInv(text, g(get).employees().employed(), g(get).employees().needed());
                }
            }.hh(SPRITES.icons().s.human).hoverTitleSet(blueprint.employment().title).hoverInfoSet(¤¤EMPLOYEES_DESC);


            s.add(r);

            if (blueprint.employmentExtra() != null) {
                r = new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, g(get).employees().efficiency());

                    }

                    @Override
                    public void hoverInfoGet(GBox b) {
                        b.title(RoomEmploymentIns.¤¤Workload);
                        b.text(RoomEmploymentIns.¤¤WorkloadD);
                        b.NL();
                        b.textL(DicTime.¤¤Today);
                        b.add(GFORMAT.perc(b.text(), g(get).employees().efficiencySoFar()));
                    };

                }.hh(SPRITES.icons().s.cog);
                s.addRightC(60, r);
                r = new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.perc(text, g(get).employees().proximity());

                    }

                    @Override
                    public void hoverInfoGet(GBox b) {
                        b.title(RoomEmploymentIns.¤¤Proximity);
                        b.text(RoomEmploymentIns.¤¤ProximityD);
                        b.NL();
                        b.textL(DicTime.¤¤Today);
                        b.add(GFORMAT.perc(b.text(), g(get).employees().proximitySoFar()));
                    };

                }.hh(SPRITES.icons().s.wheel);
                s.addRightC(60, r);
            }

            RoomEmploymentSimple ee = blueprint.employment();
            for (RoomEquip w : ee.tools()) {
                r = new GStat() {

                    @Override
                    public void update(GText text) {
                        GFORMAT.f(text, (double)g(get).employees().toolsPerPerson(w));

                    }

                    @Override
                    public void hoverInfoGet(GBox b) {

                        RoomEmploymentIns e = g(get).employees();

                        b.add(w.info);
                        b.NL(8);

                        b.textLL(b.text().lablify().add(Dic.¤¤Target).para(Dic.¤¤global));
                        b.tab(7);
                        b.add(GFORMAT.iofk(b.text(), w.target(ee).get(), w.target(ee).max()));
                        b.NL();

                        b.textLL(Dic.¤¤Current);
                        b.tab(7);
                        b.add(GFORMAT.iofk(b.text(), e.tools(w), e.toolsTarget(w)));
                        b.NL();

                        b.textLL(b.text().lablify().add(Dic.¤¤Degrade).para(DicTime.¤¤Day));
                        b.tab(7);
                        b.add(GFORMAT.f0(b.text(), e.tools(w)*w.degradePerDay));

                        b.sep();

                        double v = w.boost(blueprint.employment()).booster.getValue(e.toolD(w));

                        w.boost(blueprint.employment()).booster.hover(b, v);
                        w.boost(blueprint.employment()).booster.hoverSpan(b, v);
                        b.NL(8);

                        if (S.get().debug) {

                            b.NL();
                            b.text("res " + e.toolReserved(w));
                            b.NL();
                            b.text("need " + e.toolsNeeded(w));
                            b.NL();
                            b.text("target " + e.toolsTarget(w));
                            b.NL();
                            b.text("expire " + e.toolsToExpire(w));
                            b.NL();



                        }

                    };

                }.hh(w.resource.icon().small);
                s.addRightC(60, r);
            }






            INTE t = new INTE() {

                @Override
                public int min() {
                    return 0;
                }

                @Override
                public int max() {
                    return g(get).employees().max();
                }

                @Override
                public int get() {
                    return g(get).employees().needed();
                }

                @Override
                public void set(int t) {
                    g(get).employees().neededSet(t);
                }
            };

            // This is the slider for the actual count of workers.
            GSliderInt m = new GSliderInt(t, 160, true) {
                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    text.title(blueprint.employment().title);
                    super.hoverInfoGet(text);
                    text.NL(4);
                    text.text(¤¤EMPLOYEES_DESC);
                }
            };
            s.addRelBody(4, DIR.S, m);

            r = new GButt.ButtPanel(SPRITES.icons().s.menu) {
                @Override
                protected void clickA() {
                    VIEW.s().ui.subjects.showProfession(g(get));
                }

                @Override
                protected void renAction() {
                    activeSet(g(get).employees().employed() > 0);
                }
            }.repetativeSet(true).hoverInfoSet(¤¤WORKERS_INSPECT);
            s.addRightC(12, r);

            if (STATS.MULTIPLIERS().OVERTIME.canMark(blueprint)) {

                s.addRightC(8, cm(STATS.MULTIPLIERS().OVERTIME, get));
                s.addRightC(0, cm(STATS.MULTIPLIERS().DAY_OFF, get));

            }

            if ((blueprint instanceof ROOM_EMPLOY_AUTO)) {
                CLICKABLE c = new GButt.ButtPanel(UI.icons().s.cog) {
                    @Override
                    protected void clickA() {
                        boolean b = ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get());
                        if (!b && ((RoomInstance)get.get()).employees().needed() == 0)
                            ((RoomInstance)get.get()).employees().neededSet(1);

                        ((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get(), !b);
                    }
                    @Override
                    protected void renAction() {
                        selectedSet(((ROOM_EMPLOY_AUTO)get.get().blueprint()).autoEmploy(get.get()));
                    }
                }.hoverTitleSet(¤¤AUTO).hoverInfoSet(¤¤AUTO_DESC);
                s.addRightC(8, c);

            }

            s.body().incrH(8);

            section.addRelBody(8, DIR.S, s);



        }

        private RoomInstance g(GETTER<RoomInstance> g) {
            return (RoomInstance) g.get();
        }



    }

    private CLICKABLE cm (StatMultiplierAction mm, GETTER<RoomInstance> get) {


        CLICKABLE c = new GButt.ButtPanel(mm.icon) {

            boolean someactive;
            boolean someEmployed;

            @Override
            protected void renAction() {

                someactive = false;
                someEmployed = get.get().employees().employed() > 0;
                for (Humanoid h : get.get().employees().employees()) {
                    if (!mm.canBeMarked(h.indu()))
                        someactive = true;
                }

                activeSet(someEmployed);
                selectedSet(someactive);
            };

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;

                b.title(mm.name);
                b.text(mm.desc);
            }

            @Override
            protected void clickA() {
                for (Humanoid h : get.get().employees().employees()) {
                    mm.mark(h, !someactive);
                }
            }

        };
        return c;
    }



}
