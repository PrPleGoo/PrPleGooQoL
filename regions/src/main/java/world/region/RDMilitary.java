package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.config.Config;
import init.race.Race;
import init.sprite.UI.UI;
import init.text.D;
import settlement.entity.ENTETIES;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import util.info.INFO;
import world.WORLD;
import world.army.WDIV;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.RData.RDataE;

public class RDMilitary {

    private static CharSequence ¤¤garrisonD = "Troops that are stationed in a region which will defend it against attacks.";
    private static CharSequence ¤¤conscriptD = "Conscripts are candidates that can be trained into soldiers.";

    static {
        D.ts(RDMilitary.class);
    }

    public final RDataE garrison;
    public final DOUBLE_OE<Region> fort;
    public final Boostable bgarrison;
    public final Boostable conscriptTarget;
    public final Boostable bFortification;
    private final RDMilitaryGar gar = new RDMilitaryGar();

    RDMilitary(RDInit init){

        bgarrison = BOOSTING.push("GARRISON", 0, Dic.¤¤garrison, ¤¤garrisonD, UI.icons().s.shield,  BoostableCat.ALL().WORLD);
        bFortification = BOOSTING.push("FORTIFICATION", 8, Dic.¤¤Fort, Dic.¤¤FortD, UI.icons().s.degrade,  BoostableCat.ALL().WORLD);

        INT_OE<Region> dd = init.count.new DataShort("GARRISON", null, Config.BATTLE.REGION_MAX_DIVS*Config.BATTLE.MEN_PER_DIVISION) {
            @Override
            public int get(Region t) {
                if (FACTIONS.player().capitolRegion() == t) {
                    int pow = 0;
                    for (WDIV d : gar.player()) {
                        pow += d.men();
                    }
                    return pow;
                }
                return super.get(t);
            }

            @Override
            public void set(Region t, int s) {
                gar.init();
                super.set(t, s);
            }
        };
        garrison = new RDataE("GARRISON", dd, init, Dic.¤¤garrison);
        fort = init.count.new DataDouble("REG_FORT");

        init.upers.add(new RDUpdatable() {
            private final double dt = 2.0/(TIME.secondsPerDay);

            @Override
            public void update(Region reg, double time) {

                int t = garrisonTarget(reg);

                if (WORLD.BATTLES().besigedTime(reg) > 0) {
                    if (false) {
                        //have the garrison decrease unconditionally here...
                    }
                    int d = (int) (garrisonTarget(reg)*(1.0-besigeMul(reg)));
                    d = CLAMP.i(d, 0, t);
                    if (d < garrison.get(reg)) {
                        garrison.set(reg, d);
                    }
                    return;
                }


                int d = garrisonTarget(reg);



                garrison.moveTo(reg, time*dt*50, d);
                double f = bFortification.get(reg)-fort.getD(reg);
                double nn = fort.getD(reg) + time*dt*f;
                if (f < 0) {
                    nn += 3*time*dt*f;
                    nn = Math.max(nn, 0);
                }else
                    nn = Math.min(nn, bFortification.get(reg));
                fort.setD(reg,nn);



            }

            @Override
            public void init(Region reg) {
                garrison.set(reg, (int)garrisonTarget(reg));
                fort.setD(reg, bFortification.get(reg));
            }
        });

        conscriptTarget = BOOSTING.push("CONSCRIPTABLE_TARGET", 0, Dic.¤¤Conscripts, ¤¤conscriptD, UI.icons().s.sword,  BoostableCat.ALL().WORLD);

        new RBooster(new BSourceInfo(Dic.¤¤Population, UI.icons().s.human), 0, 20000, false) {

            @Override
            public double get(Region t) {
                return RD.RACES().popSize(t)*0.5;
            }

        }.add(conscriptTarget);

//		new RBooster(new BSourceInfo(Dic.¤¤Besiege, UI.icons().s.degrade), 1, 0, true) {
//
//			@Override
//			public double get(Region t) {
//				return besigeMul(t);
//			}
//
//		}.add(bgarrison);

    }

    public double besigeMul(Region t) {
        return CLAMP.d((WORLD.BATTLES().besigedTime(t)-TIME.secondsPerDay)/(TIME.secondsPerDay*16.0), 0, 1);
    }

    public int defensePower(Region reg) {
        return (int) Math.ceil((1+fort.getD(reg)) * power.getD(reg));
    }

    public int garrisonTarget(Region reg) {
        if (reg == null)
            return 0;
        if (reg.faction() == FACTIONS.player()) {
            return (int) bgarrison.get(reg);
        }else {
            double dz = RD.RACES().population.get(reg)/(double)ENTETIES.MAX;
            dz *= 1 + 0.25*(-8 + RD.RAN().get(reg, 9, 4))/8.0;
            dz = CLAMP.d(dz, 0, 1);

            dz *= CLAMP.d(STATS.POP().POP.data().get(null)/8000.0, 0.1, 1);


            if (reg.faction() instanceof FactionNPC) {
                FactionNPC f = (FactionNPC) reg.faction();
                dz *= 1.0 + 0.5*f.court().king().garrison();
            }
            dz = CLAMP.d(dz, 0, 1);
            dz = (100 + dz*(garrison.max(reg)-100));




            return (int) dz;
        }
    }



    public int conscripts(Race r, Faction f) {
        if (f == FACTIONS.player()) {
            if (RD.RACES().get(r) == null) {
                return 0;
            }


            int am = 0;
            for (int i = 0; i < f.realm().regions(); i++) {
                Region rr = f.realm().region(i);
                if (!rr.capitol() && RD.RACES().population.get(rr) > 0) {
                    am += conscriptTarget.get(rr)*RD.RACES().get(r).pop.get(rr)/RD.RACES().population.get(rr);
                }
            }


            return am;

        }else {
            if (RD.RACES().get(r) == null) {
                double d = (double)(f.realm().all().size()/8.0);
                d = CLAMP.d(d, 0, 1);
                return (int) (WORLD.camps().current(f, r)*d);
            }

            FactionNPC ff = (FactionNPC) f;
            double dist = 1 + CLAMP.d(RD.DIST().distance(ff)/512.0, 0, 2);

            double p = 1 + f.realm().all().size()*0.025;
            double d = STATS.POP().POP.data().get(null)/12000.0;
            d = CLAMP.d(d, 0.1, 1);
            d *= dist*(RD.RACES().get(r).pop.faction().get(f)*0.15/p);
            if (r == ff.court().king().roy().induvidual.race())
                return (int) (10 +  d);
            return (int) d;
        }
    }

    public final DOUBLE_O<Region> power = new DOUBLE_O<Region>() {
        private final INFO info = new INFO(Dic.¤¤Garrison,
                Dic.¤¤GarrisonD);

        @Override
        public double getD(Region t) {
            int p = 0;
            for (WDIV d : divisions(t))
                p += d.provess();
            return p;
        }

        @Override
        public INFO info() {
            return info;
        }
    };

    public double garrison(Region reg) {
        return garrison.getD(reg);
    }



    public void extractSpoils(Region r, int[] equipAmounts) {
        if (FACTIONS.player().capitolRegion() == r) {
            gar.extractLostEquipment(equipAmounts);
        }
    }


    public LIST<WDIV> divisions(Region r){

        return gar.divisions(r, garrison.get(r), garrisonTarget(r));

    }




}