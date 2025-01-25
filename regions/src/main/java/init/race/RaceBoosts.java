package init.race;

import java.util.Arrays;
import java.util.HashMap;

import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.sprite.UI.UI;
import settlement.army.div.Div;
import settlement.main.SETT;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

public final class RaceBoosts {

    private double[][] priorities;
    private double[][] skillRelative;
    private double[][] skill;

    public final BoostSpecs boosters = new BoostSpecs(RACES.name(), UI.icons().s.citizen, true);
    private final KeyMap<BV> bvmap = new KeyMap<BV>();


    RaceBoosts(){

        ACTION a = new ACTION() {

            @Override
            public void exe() {

                for (Race c : RACES.all()) {




                    for (BoostSpec s : c.boosts.all()) {
                        String k = s.boostable.key + s.booster.isMul;
                        if (!bvmap.containsKey(k)) {
                            bvmap.put(k, new BV(boosters, s.boostable, s.booster.isMul));
                        }
                        bvmap.get(k).set(c, s.booster.to());
                    }

                }
                setPrio();

            }
        };
        BOOSTING.connecter(a);


    }

    public BoostSpec pushIfDoesntExist(Race c, double v, Boostable bo, boolean isMul) {
        String k = bo.key + isMul;
        double none = isMul ? 1 : 0;
        if (v == none || (bvmap.containsKey(k) && bvmap.get(k).dd[c.index()] == v))
            return null;

        boolean ret = false;
        if (!bvmap.containsKey(k)) {
            ret = true;
            bvmap.put(k, new BV(boosters, bo, isMul));

        }

        BV bv = bvmap.get(k);
        bv.set(c, v);
        setPrio();
        //setRel();
        return ret ? bv.spec : null;
    }


    void setPrio() {

        HashMap<String, RoomEmploymentSimple> map = new HashMap<>();

        for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
            if (p.blueprint().bonus() != null) {
                map.put(p.blueprint().bonus().key, p);
            }
        }

        {

            double max = 0;
            double min = 10000;

            priorities = new double[RACES.all().size()][SETT.ROOMS().employment.ALLS().size()];
            for (Race r : RACES.all()) {

                for (BoostSpec s : r.boosts.all()) {
                    RoomEmploymentSimple e = map.get(s.boostable.key);
                    if (e == null)
                        continue;
                    double v = s.booster.isMul ? (s.booster.to()-1)*5 : s.booster.to();
                    max = Math.max(v, max);
                    min = Math.min(min, v);
                }
            }

            for (Race r : RACES.all()) {
                double[] vv = priorities[r.index()];
                Arrays.fill(vv, 0.5);
                if (min > max) {
                    continue;
                }
                for (RoomEmploymentSimple p : SETT.ROOMS().employment.ALLS()) {
                    if (p.blueprint().bonus() != null) {
                        double v = r.bvalue(p.blueprint().bonus());
                        v -= min;
                        v /= (max-min);
                        vv[p.eindex()] = v;
                    }
                }


            }


        }





        skillRelative = new double[RACES.all().size()][SETT.ROOMS().employment.ALLS().size()];
        skill = new double[RACES.all().size()][SETT.ROOMS().employment.ALLS().size()];

        for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {

            if (e.blueprint().bonus() != null) {
                Boostable bo = e.blueprint().bonus();
                double ave = 0;
                for (Race r : RACES.all()) {
                    ave += r.bvalue(bo)-1;
                }
                if (ave != 0)
                    ave /= RACES.all().size();
                ave += 1;

                for (Race r : RACES.all()) {
                    double[] vv = skillRelative[r.index()];
                    double v = r.bvalue(bo)/ave;
                    v = CLAMP.d(v, 0, 2);
                    v /= 2;

                    vv[e.eindex()] = v;
                    skill[r.index()][e.eindex()] = r.bvalue(bo);
                }

            }else {
                for (Race r : RACES.all()) {
                    double[] vv = skillRelative[r.index()];
                    vv[e.eindex()] = 1.0;
                    skill[r.index()][e.eindex()] = 1;
                }
            }
        }


    }

    public void debug() {

        LOG.ln("RACEBOOST");

        for (Race r : RACES.all())
            LOG.ln(r.key + " " + r.boosts.all().size());

        for (BoostSpec b : boosters.all()) {
            String s = b.boostable.key + " " + b.booster.from() + " " + b.booster.to();

            LOG.ln(s);
        }

    }


    public double getNorSkill(Race race, RoomEmploymentSimple e) {
        return skillRelative[race.index()][e.eindex()];
    }

    public double skill(Race race, RoomEmploymentSimple e) {
        return skill[race.index()][e.eindex()];
    }


    private static class BV extends Booster{

        private double from;
        private double to;
        private final double[] dd = new double[RACES.all().size()];
        private final boolean isMul;
        public final BoostSpec spec;
        private final BValue value;

        BV(BoostSpecs bos, Boostable target, boolean isMul){
            super(new BSourceInfo(RACES.name(), UI.icons().s.citizen), isMul);
            this.isMul = isMul;
            if (isMul)
                Arrays.fill(dd, 1.0);
            set();

            spec = bos.push(this, target);

            value = new BValue() {

                @Override
                public double vGet(FactionNPC f) {
                    if (f == null || f.capitolRegion() == null)
                        return 0;
                    return vGet(f.capitolRegion());
                }

                @Override
                public double vGet(Player f) {
                    return isMul ? 1.0 : 1;
                }

                @Override
                public double vGet(PopTime popTime) {
                    if (popTime.pop.race == null) {
                        double acc = 0;
                        double tot = 0;
                        for (int ri = 0; ri < RACES.all().size(); ri++) {
                            Race r = RACES.all().get(ri);
                            double pop = STATS.POP().POP.data(popTime.pop.cl).get(r);

                            acc += dd[r.index()]*pop;
                            tot += pop;
                        }
                        if (tot == 0)
                            return 0;
                        return acc/tot;

                    }else {
                        return dd[popTime.pop.race.index()];
                    }
                }

                @Override
                public double vGet(Div div) {
                    return dd[div.info.race().index()];
                }

                @Override
                public double vGet(Induvidual indu) {
                    return dd[indu.race().index()];
                }

                @Override
                public double vGet(Region reg) {
                    double acc = 0;
                    double tot = 0;
                    for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
                        RDRace r = RD.RACES().all.get(ri);
                        double pop = r.pop.get(reg);
                        acc += dd[r.race.index()]*pop;
                        tot += pop;
                    }
                    if (tot == 0)
                        return 0;
                    return acc/tot;
                }
            };
        }

        void set(Race c, double value) {
            dd[c.index()] = value;
            set();
        }

        private void set() {
            if (isMul) {
                from = 1.0;
                to = 1.0;
            }else {
                from = 0;
                to = 0;
            }

            for (double v : dd) {
                from = Math.min(v, from);
                to = Math.max(v, to);
            }
        }

        @Override
        public double getValue(double input) {
            return input;
        }

        @Override
        public double from() {
            return from;
        }

        @Override
        public double to() {
            return to;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            return o.boostableValue(value);
        }


    }


}
