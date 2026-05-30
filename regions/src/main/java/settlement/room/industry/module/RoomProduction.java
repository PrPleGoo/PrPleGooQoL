package settlement.room.industry.module;


import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.resources.ResGDrink;
import init.resources.ResGEat;
import init.sprite.UI.UI;
import init.trade.TR;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HCLASS_RACE;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.industry.module.consumption.RoomConsumption.ROOM_CONSUMPTION_HASER;
import settlement.room.industry.module.consumption.RoomConsumptionAbs;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEquip;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;
import settlement.stats.equip.EquipCivic;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.text.Dic;
import world.army.AD;
import world.army.ADSupply;
import world.region.RD;

public class RoomProduction {

    private final ArrayList<Res> producers = new ArrayList<Res>(RESOURCES.ALL().size());
    private final ArrayList<Res> consumers = new ArrayList<Res>(RESOURCES.ALL().size());
    private final ArrayList<Res> eaters = new ArrayList<Res>(RESOURCES.ALL().size());

    public RoomProduction(ROOMS rooms){
        for(RESOURCE res : RESOURCES.ALL()) {
            producers.add(new Res(res));
            consumers.add(new Res(res));
            eaters.add(new Res(res));
        }

        for (RoomBlueprint h : rooms.all()) {

            if (h instanceof INDUSTRY_HASER) {
                INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
                for (Industry ins : ii.industries()) {
                    for (IndustryResource oo : ins.outs()) {
                        SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
                        producers.get(i.res.index()).ins.add(i);
                        producers.get(i.res.index()).all.add(i);
                    }
                    for (IndustryResource oo : ins.ins()) {
                        SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
                        consumers.get(i.res.index()).ins.add(i);
                        consumers.get(i.res.index()).all.add(i);
                    }
                }
            }

            else if (h instanceof ROOM_CONSUMPTION_HASER) {
                RoomConsumptionAbs c = ((ROOM_CONSUMPTION_HASER) h).consumption();
                for (IndustryResource oo : c.ins()) {
                    SourceRC i = new SourceRC(oo.resource, (RoomBlueprintImp)h);
                    consumers.get(i.res.index()).ins.add(i);
                    consumers.get(i.res.index()).all.add(i);
                }

            }
        }


        for (ResGEat e : RESOURCES.EDI().all()) {

            Source s = new Source(e.resource) {

                @Override
                public double am() {
                    return FACTIONS.player().res().out(RTYPE.CONSUMED).history(TR.get(e.resource)).get(1);
                }

                @Override
                public SPRITE icon() {
                    return UI.icons().s.human;
                }

                @Override
                public CharSequence name() {
                    return Dic.¤¤Consumed;
                }

            };

            consumers.get(e.resource.index()).all.add(s);
            eaters.get(e.resource.index()).all.add(s);
        }

        for (ResGDrink e : RESOURCES.DRINKS().all()) {

            Source s = new Source(e.resource) {

                @Override
                public double am() {
                    return FACTIONS.player().res().out(RTYPE.CONSUMED).history(TR.get(e.resource)).get(1);
                }

                @Override
                public SPRITE icon() {
                    return UI.icons().s.human;
                }

                @Override
                public CharSequence name() {
                    return Dic.¤¤Consumed;
                }

            };

            consumers.get(e.resource.index()).all.add(s);
            eaters.get(e.resource.index()).all.add(s);
        }

        for (RESOURCE res : RESOURCES.ALL()) {
            producers.get(res.index()).all.add(new RegProduction(res));
            consumers.get(res.index()).all.add(new RegConsumption(res));

            consumers.get(res.index()).all.add(new Source(res) {

                @Override
                public double am() {
                    return SETT.MAINTENANCE().estimateGlobal(res);
                }

                @Override
                public SPRITE icon() {
                    return SETT.MAINTENANCE().icon;
                }

                @Override
                public CharSequence name() {
                    return Dic.¤¤Maintenance;
                }

            });



            consumers.get(res.index()).all.add(new Source(res) {

                @Override
                public double am() {
                    double d = res.degradeSpeed()/(TIME.years().bitConversion(TIME.days())*BOOSTABLES.CIVICS().SPOILAGE.get(HCLASS_RACE.clP(null, null)));
                    double am = d*SETT.ROOMS().STOCKPILE.tally().amountTotal(res)*0.5;
                    am += d*SETT.ROOMS().HAULER.tally.amountTotal(res);
                    am += d*SETT.ROOMS().EXPORT.tally.amount.get(res);
                    am += d*SETT.ROOMS().IMPORT.tally.amount.get(res);
                    return am;
                }

                @Override
                public SPRITE icon() {
                    return SETT.MAINTENANCE().icon;
                }

                @Override
                public CharSequence name() {
                    return Dic.¤¤Spoilage;
                }

            });
        }



        for (RoomEquip e : rooms.employment.equip.ALL) {
            consumers.get(e.resource.index()).all.add(new Source(e.resource) {

                @Override
                public double am() {
                    return e.currentTotal()*e.degradePerDay;
                }

                @Override
                public SPRITE icon() {
                    return UI.icons().s.hammer;
                }

                @Override
                public CharSequence name() {
                    return Dic.¤¤Equipped;
                }

            });
        }

        BOOSTING.connecter(new ACTION() {

            @Override
            public void exe() {
                for (EquipCivic e : STATS.EQUIP().civics()) {

                    consumers.get(e.resource.index()).all.add(new Source(e.resource) {

                        @Override
                        public double am() {
                            return e.stat().data().get(null)*e.wearPerYear/TIME.years().bitConversion(TIME.days());
                        }

                        @Override
                        public SPRITE icon() {
                            return UI.icons().s.citizen;
                        }

                        @Override
                        public CharSequence name() {
                            return Dic.¤¤Equipped;
                        }

                    });

                }

                for (ADSupply sup : AD.supplies().all) {
                    consumers.get(sup.res.index()).all.add(new Source(sup.res) {

                        @Override
                        public double am() {

                            if (!SETT.ROOMS().SUPPLY.has(sup.res))
                                return 0;

                            return sup.consumedPerDayCurrent(FACTIONS.player());
                        }

                        @Override
                        public SPRITE icon() {
                            return UI.icons().s.sword;
                        }

                        @Override
                        public CharSequence name() {
                            return Dic.¤¤Supplies;
                        }

                    });
                }


                for (RES_AMOUNT e : RACES.res().homeResMax(null)) {

                    final ArrayListGrower<HCLASS_RACE> con = new ArrayListGrower<>();
                    final ArrayListGrower<Integer> conRI = new ArrayListGrower<>();

                    for (HCLASS cl : HCLASSES.ALL()) {
                        for (Race ra : RACES.all()) {
                            int ri = 0;
                            for (RES_AMOUNT rr : ra.home().clas(cl).resources()) {


                                if (rr.resource() == e.resource()) {
                                    con.add(HCLASS_RACE.clP(ra, cl));
                                    conRI.add(ri);
                                }
                                ri++;
                            }
                        }
                    }

                    consumers.get(e.resource().index()).all.add(new Source(e.resource()) {

                        @Override
                        public double am() {
                            double am = 0;
                            for (int i = 0; i < con.size(); i++) {
                                am += STATS.HOME().current(con.getC(i).cl, con.get(i).race, conRI.get(i))*STATS.HOME().rate(con.getC(i).cl, con.get(i).race);
                            }

                            return am/TIME.years().bitConversion(TIME.days());
                        }

                        @Override
                        public SPRITE icon() {
                            return UI.icons().s.house;
                        }

                        @Override
                        public CharSequence name() {
                            return STATS.HOME().materials.info().name;
                        }

                    });
                }

                for (ROOM_TEMPLE t : SETT.ROOMS().TEMPLES.ALL) {
                    if (t.resource != null) {
                        consumers.get(t.resource.index()).all.add(new Source(t.resource) {

                            @Override
                            public double am() {
                                double a = 0;
                                for (int i = 0; i < t.instancesSize(); i++) {
                                    a += t.getInstance(i).sacrifices();
                                }
                                return a;
                            }

                            @Override
                            public SPRITE icon() {
                                return t.icon;
                            }

                            @Override
                            public CharSequence name() {
                                return t.info.names;
                            }

                        });
                    }

                }


                for (Res r : producers)
                    r.init();
                for (Res r : consumers)
                    r.init();
            }
        });


    }

    private void update(int ticks) {

        upI = GAME.updateI();
        ENTITY[] es = SETT.ENTITIES().getAllEnts();

        int tott = ticks*200;
        if (tott < 0 || tott > es.length)
            tott= es.length;

        for (int i = 0; i < tott; i++) {

            if (ui >= es.length) {

                for (int ii = 0; ii < producers.size(); ii++) {
                    Res r = producers.get(ii);
                    double tot = 0;
                    for (SourceR in : r.ins) {
                        in.am = in.old;
                        in.old = 0;
                        tot += in.am;
                    }
                    for (Source in : r.all) {
                        if (in instanceof RegProduction)
                            tot += in.am();
                    }
                    r.am = tot;
                }
                for (int ii = 0; ii < consumers.size(); ii++) {
                    Res r = consumers.get(ii);
                    double tot = 0;
                    for (SourceR in : r.ins) {
                        in.am = in.old;
                        in.old = 0;
                        tot += in.am;
                    }

                    r.am = tot;
                }

                ui = 0;
                break;
            }

            ENTITY e = es[ui];
            ui++;
            if (e != null && e instanceof Humanoid) {
                Humanoid h = (Humanoid) e;
                RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
                if (ins == null)
                    continue;

                if (ins instanceof ROOM_PRODUCER_INSTANCE) {
                    ROOM_PRODUCER_INSTANCE p = (ROOM_PRODUCER_INSTANCE) ins;

                    Industry in = p.industry();
                    for (IndustryResource oo : in.outs()) {
                        RESOURCE res = oo.resource;
                        double d = p.productionRate(ins, h, in, oo);
                        for (int ri = 0; ri < producers.get(res.index()).ins.size(); ri++) {
                            SourceR ii = producers.get(res.index()).ins.get(ri);
                            if (ii.blue == in.blue && ii.ins == in) {
                                ii.old += d;
                            }
                        }
                    }
                    for (IndustryResource oo : in.ins()) {
                        RESOURCE res = oo.resource;
                        double d = in.consumptionRate(ins, h, oo);
                        for (int ri = 0; ri < consumers.get(res.index()).ins.size(); ri++) {
                            SourceR ii = consumers.get(res.index()).ins.get(ri);
                            if (ii.blue == in.blue && ii.ins == in) {
                                ii.old += d;
                            }
                        }
                    }
                }

                if (ins.blueprintI()  instanceof ROOM_CONSUMPTION_HASER) {
                    RoomConsumptionAbs in = ((ROOM_CONSUMPTION_HASER) ins.blueprintI()).consumption();
                    for (int rii = 0; rii < in.ins().size(); rii++) {
                        IndustryResource oo = in.ins().get(rii);
                        RESOURCE res = oo.resource;
                        double d = in.consumptionRate(ins, h, oo);
                        for (int ri = 0; ri < consumers.get(res.index()).ins.size(); ri++) {
                            SourceR ii = consumers.get(res.index()).ins.get(ri);
                            if (ii.blue == in.blue) {
                                ii.old += d;
                            }
                        }
                    }

                }

            }


        }
    }

    int ui = 0;
    int upI = 0;

    public double produced(RESOURCE res) {
        if (Math.abs(upI-GAME.updateI()) > 1) {
            update(Math.abs(upI-GAME.updateI()));
        }
        return producers.get(res.index()).am;
    }

    public double consumed(RESOURCE res) {
        if (Math.abs(upI-GAME.updateI()) > 1) {
            update(Math.abs(upI-GAME.updateI()));
        }
        return consumers.get(res.index()).am;
    }

    public LIST<Source> producers(RESOURCE res) {
        return producers.get(res.index()).all;
    }

    public LIST<Source> consumers(RESOURCE res) {
        return consumers.get(res.index()).all;
    }

    public LIST<Source> eaters(RESOURCE res) {
        return eaters.get(res.index()).all;
    }

    private class Res {

        private final ArrayListGrower<Source> all = new ArrayListGrower<>();
        private final ArrayListGrower<SourceR> ins = new ArrayListGrower<>();
        private double am;

        Res(RESOURCE res){

        }

        private void init() {
            for (int i1 = 0; i1 < ins.size(); i1++) {
                for (int i2 = 0; i2 < ins.size(); i2++) {
                    if (i2 == i1)
                        continue;
                    if (ins.get(i1).blue == ins.get(i2).blue) {
                        ins.get(i1).multiple = true;
                        ins.get(i2).multiple = true;
                    }

                }

            }
        }

    }



    public abstract static class Source {

        public final RESOURCE res;


        Source(RESOURCE res){
            this.res = res;
        }

        public abstract double am();

        public Industry thereAreMultipleIns() {
            return null;
        }

        public abstract SPRITE icon();

        public abstract CharSequence name();

    }


    public class RegProduction extends Source{

        RegProduction(RESOURCE res){
            super(res);

        }

        @Override
        public double am() {
            int am = 0;
            for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
                int aa = RD.OUTPUT().get(TR.get(res)).getDelivery(FACTIONS.player().realm().region(i));
                am += aa;

            }
            return am;
        }

        @Override
        public Industry thereAreMultipleIns() {
            return null;
        }

        @Override
        public SPRITE icon() {
            return UI.icons().s.money;
        }

        @Override
        public CharSequence name() {
            return  Dic.¤¤Region.toString() + " " + Dic.¤¤Production.toString();
        }

    }

    public class RegConsumption extends Source{

        RegConsumption(RESOURCE res){
            super(res);
        }

        @Override
        public double am() {
            int am = 0;
            for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
                int aa = (int) RD.INPUTS().get(res).get(FACTIONS.player().realm().region(i));
                am += aa;

            }
            return am;
        }

        @Override
        public Industry thereAreMultipleIns() {
            return null;
        }

        @Override
        public SPRITE icon() {
            return UI.icons().s.money;
        }

        @Override
        public CharSequence name() {
            return  Dic.¤¤Region.toString() + " " + Dic.¤¤Consumed.toString();
        }
    }


    public class SourceR extends Source{

        private final RoomBlueprintImp blue;
        private final Industry ins;
        private double old;
        private double am;
        private boolean multiple = false;

        SourceR(RESOURCE res, RoomBlueprintImp blue, Industry ins){
            super(res);
            this.blue = blue;
            this.ins = ins;
        }

        @Override
        public double am() {
            if (Math.abs(upI-GAME.updateI()) > 1) {
                update(Math.abs(upI-GAME.updateI()));

            }
            return am;
        }

        @Override
        public Industry thereAreMultipleIns() {
            return multiple ? ins : null;
        }

        @Override
        public SPRITE icon() {
            return blue.icon.small;
        }

        @Override
        public CharSequence name() {
            return blue.info.names;
        }

    }

    public class SourceRC extends SourceR{


        SourceRC(RESOURCE res, RoomBlueprintImp blue){
            super(res, blue, null);
        }

    }


}
