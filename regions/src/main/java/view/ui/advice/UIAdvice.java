package view.ui.advice;

import java.io.IOException;
import java.util.ArrayList;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.player.PBonusSetting;
import game.save.PROP;
import game.save.Savable;
import game.time.TIME;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.tech.TECHS;
import init.tech.TechCurrency;
import init.trade.TR;
import init.trade.TRADABLE;
import init.type.HCLASSES;
import init.type.HCLASS_RACE;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.IndustryResource;
import settlement.room.infra.admin.AdminData.ROOM_ADMIN_HOLDER;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.employment.RoomEmployment;
import settlement.stats.POP;
import settlement.stats.STATS;
import settlement.stats.service.StatServiceRoom;
import settlement.stats.standing.STANDINGS;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageSection;
import view.ui.message.MessageText;
import view.world.generator.WorldViewGenerator;
import world.region.RD;

public class UIAdvice extends GuiSection{

    private static CharSequence ¤¤name = "Potential Problems";

    private static CharSequence ¤¤toggleOff = "Click to suppress this warning.";
    private static CharSequence ¤¤toggleOn = "Click to activate this warning";

    private static CharSequence ¤¤resettle = "Resettle";
    private static CharSequence ¤¤resettleD = "If we find that our city is crumbling into dust, we can move to a different part of Syx and start over. We will retain our current level, and be able to unlock any new titles. We will also keep some of our resources.";
    private static CharSequence ¤¤resettleSure = "Are you sure you want to abandon your current city and try your luck somewhere else?";


    private static CharSequence ¤¤Oddjobbers = "While oddjobbers are handy as builders and lending a hand where possible, they are essentially freeloaders, only draining your realm of resources without giving anything back. Make sure they are either contributing to your needs, or producing something to export.";
    private static CharSequence ¤¤OddjobbersG = "The amount of odd-jobber you have is currently not a problem.";
    private static CharSequence ¤¤OddjobbersB = "You have a lot of oddjobbers compared to your workforce. Give them something to do.";

    private static CharSequence ¤¤Work = "Make sure that all rooms are making the most out of their employees. If they are not properly configured, the employees will idle and be no better than oddjobbers - a drain on your economy.";
    private static CharSequence ¤¤WorkG = "No rooms currently have severe problems with workload.";
    private static CharSequence ¤¤WorkB = "Some rooms have problems with workload.";

    private static CharSequence ¤¤Resources = "Resources are meant to be used. Only a few of them are beneficial to store, like food and weapons, but only in moderate amounts. Stored resources degrade and spoil, as well as attract unwanted attention to your city. If goods can't be consumed domestically, they should be traded for something that can.";
    private static CharSequence ¤¤ResourcesG = "You do not currently store any goods in excess.";
    private static CharSequence ¤¤ResourcesB = "Some of your goods are stored excessively, currently rotting away in storage for nothing.";

    private static CharSequence ¤¤Money = "Money itself can not be eaten or utilized. It is but a temporary credit to be traded into something that is actually beneficial to your citizens. And like with resources, if horded, you are only wasting the potential of your prosperity through inflation and attracting hostile attention to your city.";
    private static CharSequence ¤¤MoneyG = "You currently are not hording much wealth.";
    private static CharSequence ¤¤MoneyB = "Your treasury is full. Piled up denarii fills no stomach. You should use it.";

    private static CharSequence ¤¤Research = "Research and technology can be just as detrimental as beneficial. It's not simply about advancing. Since technology has a perpetual cost in one form or the other, it's important to ask yourself if a technology is actually benefitting your economy, if another allocation is better, or if the resources should be diverted elsewhere.";
    private static CharSequence ¤¤ResearchG = "You are not spending excessive resources on technology. But remember to make sure you are getting the most out of your allocations.";
    private static CharSequence ¤¤ResearchB = "You have a very high ratio of research workers compared to your industry workers. This could indicate that you'd be better off reassigning some research workers to industries instead.";

    private static CharSequence ¤¤Service = "Having services always cost you resources one way or the other. As such, it's important not to over-dimension them. Remember to also go for the most rewarding services first by measuring their cost vs their benefit.";
    private static CharSequence ¤¤ServiceG = "Your services are properly dimensioned.";
    private static CharSequence ¤¤ServiceB = "Some of your services have low usage and are thus over-dimensioned";

    private static CharSequence ¤¤Nobles = "Nobles are crucial to your progress, offering big boosts to your subjects. Make sure you use all available slots and promotions.";
    private static CharSequence ¤¤NoblesG = "All nobles are assigned.";
    private static CharSequence ¤¤NoblesB = "You have free slots / promotions for nobles.";

    private static CharSequence ¤¤Trade = "You are able to trade wares att any price, but it's in your interest to buy low and sell high. You can control this by setting max or min prices in the trade panel.";
    private static CharSequence ¤¤TradeG = "You are trading all goods at reasonable prices";
    private static CharSequence ¤¤TradeB = "You are trading some goods at exorbitant prices.";

    private static CharSequence RegionSupplies = "Regions can require goods for their operations. When you have a supply deficit, all regions suffer. Regional Supply Depots in the capital work to solve these deficits. Build a depot or investigate your logistics if you are suffering from deficits.";
    private static CharSequence RegionSuppliesG = "All your regions have suffient supplies";
    private static CharSequence RegionSuppliesB = "Your regions are suffering from supply shortages.";


    private static CharSequence ¤¤mAdvice = "Advice";
    private static CharSequence ¤¤mAdviceD = "You have some helpful advice to look through. It's located at the top-right portion of the city UI view.";

    static {
        D.ts(UIAdvice.class);
    }

    private final ArrayListGrower<Entry> all = new ArrayListGrower<Entry>();
    public static boolean resettlePossible = true;

    private boolean hasOffered = false;
    private boolean hasAdvice = false;
    private static boolean silence = false;

    private static final ACTION restart = new ACTION() {

        @Override
        public void exe() {

            String race = FACTIONS.player().race().key;
            PBonusSetting ss = FACTIONS.player().bonusesCustom;
            ss.startLevel = FACTIONS.player().level().current().index();

            for (RESOURCE res: RESOURCES.ALL()) {
                int am = SETT.ROOMS().STOCKPILE.tally().amountTotal(res)/4;
                ss.startResources.set(res, am);
            }

            boolean a = GAME.achieving();

            CORE_STATE.Constructor c = new CORE_STATE.Constructor() {

                @Override
                public CORE_STATE getState() {
                    String[] sc = GAME.script().currentScripts();
                    CORE_STATE s = GAME.create(sc);

                    return s;
                }

                @Override
                public void doAfterSet() {
                    FACTIONS.player().bonusesCustom.copy(ss);
                    GAME.achieve(a);
                    WorldViewGenerator.setresettle(RACES.map().tryGet(race));
                }

            };

            CORE.setCurrentState(c);

        }
    };

    public static GButt.ButtPanel make() {
        UIAdvice a = new UIAdvice();
        resettlePossible = true;

        GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.advice) {

            @Override
            protected void clickA() {
                VIEW.inters().popup.show(a, this);
            }

            @Override
            public void hoverInfoGet(GUI_BOX text) {
                GBox b = (GBox) text;

                b.title(¤¤name);
                for (Entry e : a.all) {
                    if (e.toggled()) {
                        if (e.is()) {
                            b.error(e.bad);
                        }
                    }


                }
            }


            @Override
            protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
                                  boolean isHovered) {

                super.render(r, ds, isActive, isSelected, isHovered);
                if (silence)
                    return;
                boolean has = false;
                for (Entry e : a.all) {
                    if (e.toggled() && e.is()) {

                        OPACITY.O25TO100.bind();
                        COLOR.REDISH.renderFrame(r, body, -4, 4);
//
//						UI.icons().s.alert.render(r, body.x1()+3, body.y1()+3);
//						COLOR.unbind();
                        OPACITY.unbind();
                        has = true;
                        break;
                    }
                }
                if (has && POP.pop() > 500) {
                    if (!a.hasAdvice) {
                        a.hasAdvice = true;
                        new MessageText(¤¤mAdvice, ¤¤mAdviceD).send();
                    }



                }
                sendResettle(a);

            }


        };


        b.hoverInfoSet(¤¤name);
        return b;

    }

    private static void sendResettle(UIAdvice a) {
        if (POP.pop() < 200)
            return;
        if (a.hasOffered)
            return;
        double e = STANDINGS.CITIZEN().expectation.getD(null, 0);
        if (e < STANDINGS.CITIZEN().expectation.getD(null, 1)*0.9) {
            a.hasOffered = true;
            new MessResettle().send();
        }

        double ee = 0;
        for (int i = 1; i <= 4; i++) {
            ee += STANDINGS.CITIZEN().expectation.getD(null, i*4)/4.0;
        }

        if (e/ee > 0.9 && e/ee < 1.1) {
            a.hasOffered = true;
            new MessResettle().send();
        }

    }

    public static void silence() {
        silence = true;
    }

    private UIAdvice() {

        silence = false;

        new Entry(all, UI.icons().m.citizen, ¤¤Oddjobbers, ¤¤OddjobbersG, ¤¤OddjobbersB) {

            @Override
            boolean is() {
                double all = STATS.WORK().workforce();
                double emp = STATS.WORK().EMPLOYED.stat().data(HCLASSES.CITIZEN()).get(null);

                if (all > 100) {
                    if (emp/all < 0.15)
                        return true;
                }else if (all > 20) {
                    if (emp/all < 0.5)
                        return true;
                }

                return false;
            }

        };

        new Entry(all, UI.icons().m.workshop, ¤¤Work, ¤¤WorkG, ¤¤WorkB) {

            double ee = 0;
            double tot = 0;
            private final Count<RoomEmployment> count = new Count<RoomEmployment>(SETT.ROOMS().employment.ALL()) {

                @Override
                void count(RoomEmployment t) {
                    ee += t.employed()*t.efficiency();
                    tot += t.employed();

                }

                @Override
                boolean pis() {

                    if (ee > 20) {
                        if (ee/tot < 0.85) {
                            old = 0;
                            return true;

                        }else if (old < 0.85) {
                            return true;
                        }else
                            old = 1;

                    }
                    old = 1;
                    return false;
                }

                @Override
                void clear() {
                    ee = 0;
                    tot = 0;
                }

            };

            @Override
            boolean is() {
                return count.is();
            }

        };

        new Entry(all, UI.icons().m.storage_pull, ¤¤Resources, ¤¤ResourcesG, ¤¤ResourcesB) {

            double spoil = 0;
            private final Count<RESOURCE> count = new Count<RESOURCE>(RESOURCES.ALL()) {

                @Override
                void count(RESOURCE res) {
                    double stored = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res);
                    stored += SETT.ROOMS().HAULER.tally.amountReservable.getD(res);
                    stored *= SETT.RECIPES().ratesV.bestRecipe(res.tr()).manpowerTotal()*res.degradeSpeed();
                    stored /= 16.0;


                    spoil += stored;

                }

                @Override
                boolean pis() {
                    if (spoil == 0) {
                        old = 0;
                        return false;

                    }
                    double tres = spoil/(STATS.WORK().workforce()+1);
                    if (tres > 0.25) {
                        old = 1;
                        return true;
                    }else if (tres > 0.15 && old > 1)
                        return true;
                    else
                        old = 0;
                    return false;
                }

                @Override
                void clear() {
                    spoil = 0;
                }

            };

            @Override
            boolean is() {
                return count.is();
            }

        };

        new Entry(all, UI.icons().m.coins, ¤¤Money, ¤¤MoneyG, ¤¤MoneyB) {

            @Override
            boolean is() {
                return FACTIONS.player().credits().getD()/(NPCStockpile.AVERAGE_PRICE*4.0*POP.tot(HCLASSES.CITIZEN(), null) + POP.tot(HCLASSES.NOBLE(), null)) > 1;
            }

        };

        new Entry(all, UI.icons().m.admin, ¤¤Research, ¤¤ResearchG, ¤¤ResearchB) {

            private ArrayListGrower<RoomBlueprintIns<?>> all = new ArrayListGrower<RoomBlueprintIns<?>>();
            {
                for (RoomBlueprintIns<?> p : SETT.ROOMS().ins()) {
                    if (p instanceof ROOM_ADMIN_HOLDER) {
                        ROOM_ADMIN_HOLDER h = (ROOM_ADMIN_HOLDER) p;
                        for (TechCurrency cc : TECHS.COSTS()) {
                            if (cc.bo == h.admin().target) {
                                all.add(p);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            boolean is() {

                double am = 0;
                for (RoomBlueprintIns<?> blue : all) {
                    am += blue.employment().employed();
                    if (blue instanceof INDUSTRY_HASER) {
                        for (IndustryResource ii : ((INDUSTRY_HASER)blue).industries().get(0).ins()) {
                            double a = ii.history().get(1)*(1-TIME.days().bitPartOf()) + ii.history().get(0)*TIME.days().bitPartOf();
                            a *= SETT.RECIPES().ratesV.bestRecipe(ii.resource.tr()).manpowerTotal();
                            am += a;
                        }

                    }
                }

                if (am > STATS.WORK().workforce()*0.2)
                    return true;
                return false;
            }

        };

        new Entry(all, UI.icons().m.chainsFree, ¤¤Service, ¤¤ServiceG, ¤¤ServiceB) {

            private final Count<StatServiceRoom> count = new Count<StatServiceRoom>(STATS.SERVICE().ROOMS) {

                double low = 1.0;

                @Override
                void count(StatServiceRoom t) {
                    low = Math.min(t.usage, low);
                }

                @Override
                boolean pis() {
                    return low < 0.5;
                }

                @Override
                void clear() {
                    low = 1;
                }
            };

            @Override
            boolean is() {
                return count.is();
            }

        };

        new Entry(all, UI.icons().m.noble, ¤¤Nobles, ¤¤NoblesG, ¤¤NoblesB) {

            @Override
            boolean is() {
                return GAME.NOBLE().ranksAllocated()  < GAME.NOBLE().MAX_RANKS.get(HCLASS_RACE.clP()) || GAME.NOBLE().active().size() < GAME.NOBLE().MAX.get(HCLASS_RACE.clP());
            }

        };

        new Entry(all, UI.icons().m.wheel, ¤¤Trade, ¤¤TradeG, ¤¤TradeB) {

            private final Count<TRADABLE> count = new Count<TRADABLE>(TR.ALL()) {

                boolean crap = false;

                @Override
                boolean pis() {
                    return crap;
                }

                @Override
                void clear() {
                    crap = false;
                }

                @Override
                void count(TRADABLE t) {
                    if (crap)
                        return;
                    if (SETT.TRADE().buyer(t).importing() && FACTIONS.player().trade.unitsImported.history(t).get() > 0) {
                        double p = FACTIONS.player().trade.priceImported.history(t).get();
                        if (p > FACTIONS.player().trade.pricesAve.get(t)*8.0) {
                            crap = true;
                        }
                    }
                    if (SETT.TRADE().seller(t).exporting() == null && FACTIONS.player().trade.unitsExported.history(t).get() > 0) {
                        double p = FACTIONS.player().trade.priceExported.history(t).get();
                        if (p <= FACTIONS.player().trade.pricesAve.get(t)/8.0) {
                            crap = true;
                        }
                    }


                }
            };

            @Override
            boolean is() {
                return count.is();

            }

        };

        new Entry(all, UI.icons().m.gov, RegionSupplies, RegionSuppliesG, RegionSuppliesB) {
            @Override
            boolean is() {
                return RD.DEFICITS().anyDeficits();
            }

        };

        ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(all.size()+1);

        for (Entry e : all)
            rows.add(e);

        add(new GScrollRows(rows, 80*6).view());

        addRelBody(8, DIR.N, new GHeader(¤¤name));




        addRelBody(8, DIR.S, new GButt.ButtPanel(¤¤resettle) {


            @Override
            public void hoverInfoGet(GUI_BOX text) {
                text.text(¤¤resettleD);
            }

            @Override
            protected void clickA() {
                VIEW.inters().yesNo.activate(¤¤resettleSure, restart, null, true);
            }

            @Override
            protected void renAction() {
                activeSet(resettlePossible);
            }

        });

        GAME.saver().addSpecialSaver(new Savable("UI_ADVICE") {

            @Override
            protected void save(FilePutter file) {
                file.bool(hasAdvice);
                file.bool(hasOffered);

            }

            @Override
            protected void load(FileGetter file) throws IOException {
                hasAdvice = file.bool();
                hasOffered = file.bool();
            }
        });

    }

    private static abstract class Entry extends ClickableAbs{

        private final CharSequence sdesc;
        private final String key;
        private final SPRITE icon;

        private final GText good;
        private final GText bad;
        protected double old = -1;

        Entry(ArrayListGrower<Entry> all, SPRITE icon, CharSequence sdesc, CharSequence sgood, CharSequence sbad){
            super(600, 80);

            String key = "ADVICE_" + all.size();
            all.add(this);
            this.sdesc = sdesc;
            this.key = key;
            this.icon = icon;


            good = new GText(UI.FONT().S, sgood);
            good.normalify2();
            good.setMaxWidth(500);
            good.setMultipleLines(true);

            bad = new GText(UI.FONT().S, sbad);
            bad.errorify();
            bad.setMaxWidth(500);
            bad.setMultipleLines(true);




        }

        abstract boolean is();

        @Override
        protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

            GButt.ButtPanel.renderBG(r, true, PROP.propI(key, 0) == 0, isHovered, body);

            icon.renderC(r, body.x1()+24, body.cY());

            GText t = is() ? bad : good;

            t.renderCY(r, body.x1()+48, body.cY());

            GButt.ButtPanel.renderFrame(r, body);

        }

        @Override
        protected void clickA() {
            PROP.propISet(key, (PROP.propI(key, 0) + 1) & 1);
        }

        public boolean toggled() {
            return PROP.propI(key, 0) == 0;
        }

        @Override
        public void hoverInfoGet(GUI_BOX text) {
            GBox b = (GBox) text;
            b.text(sdesc);
            b.sep();
            CharSequence t = PROP.propI(key, 0) == 0 ? ¤¤toggleOff : ¤¤toggleOn;
            b.warn(t);

            super.hoverInfoGet(text);
        }

    }


    private abstract static class Count<T> {

        boolean has = false;
        int ui = 0;
        private final LIST<T> all;

        Count(LIST<T> all){
            this.all = all;
        }

        boolean is() {

            if (GAME.updateI() == ui) {
                return has;
            }

            int start = ui;
            int end = GAME.updateI()&Integer.MAX_VALUE;
            if (end-start >= all.size()) {
                start = end-all.size();
            }
            ui = end;

            for (; start < end; start++) {
                int ei = start % all.size();
                if (ei == 0) {
                    has = pis();
                    clear();
                }
                T e = all.get(ei);
                count(e);
            }
            return has;
        }

        abstract void count(T t);
        abstract boolean pis();
        abstract void clear();

    }

    private static class MessResettle extends MessageSection {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        public MessResettle() {
            super(¤¤resettle);
        }

        @Override
        protected void make(GuiSection section) {
            paragraph(¤¤resettleD);

            section.addRelBody(8, DIR.S, new GButt.ButtPanel(¤¤resettle) {


                @Override
                public void hoverInfoGet(GUI_BOX text) {
                    text.text(¤¤resettleD);
                }

                @Override
                protected void clickA() {
                    VIEW.inters().yesNo.activate(¤¤resettleSure, restart, null, true);
                }

                @Override
                protected void renAction() {
                    activeSet(resettlePossible);
                }

            });

        }


    }

}
