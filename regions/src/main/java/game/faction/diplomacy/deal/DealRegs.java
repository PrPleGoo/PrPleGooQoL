package game.faction.diplomacy.deal;


import game.boosting.BUtil;
import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.FSlaves;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.LOG;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import world.WORLD;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;
import world.region.RDOutputs;
import world.region.building.RDBuilding;
import world.region.pop.RDRace;

public final class DealRegs {

    private final ArrayList<DealReg> tmp = new ArrayList<>(128);
    private final DealReg[] all = new DealReg[128];
    private int selfWorth;
    private int offerableWorth;

    //	private final Bitmap1D selected = new Bitmap1D(WREGIONS.MAX, false);
//	private final Bitmap1D canSelect = new Bitmap1D(WREGIONS.MAX, false);
    private boolean dirty = true;

    private Faction giver;
    private Faction reciever;

    private final RegData data;
    private final Deal deal;
    private final Treaty t = new Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (to == null)
                return true;
            if (prevReg == null)
                return false;
            if (prevReg.faction() == reciever)
                return true;
            if (prevReg == to)
                return true;
            return to.faction() == giver;
        }
    };

    private final WRegSel sel = new WRegSel() {

        @Override
        public boolean is(Region t) {
            return t.faction() == giver && !t.capitol();
        }
    };


    DealRegs(Deal deal, RegData data) {
        for (int i = 0; i < all.length; i++)
            all[i] = new DealReg();
        this.data = data;
        this.deal = deal;
    }

    void init(Faction giver, Faction reciever, FactionNPC evaluator) {

        data.selected.clear();
        data.canSelect.clear();

        this.giver = giver;
        this.reciever = reciever;
        selfWorth = 0;
        for (int i = 0; i < giver.realm().regions(); i++) {
            if (giver.realm().region(i).capitol())
                continue;
            selfWorth += valueRegion(giver.realm().region(i), evaluator, 0);
            data.selected.set(giver.realm().region(i).index(), false);
        }
        tmp.clearSloppy();

        offerableWorth = 0;
        int ri = 0;

        if (reciever.capitolRegion() != null) {
            for (RegDist d : WORLD.PATH().regFinder.all(reciever.capitolRegion(), t, sel)){
                if (!tmp.hasRoom())
                    break;

                if (d.reg.capitol())
                    continue;

                DealReg rr = all[ri++];
                rr.reg = d.reg;
                if (giver == evaluator)
                    data.values[d.reg.index()] = (int) valueRegion(d.reg, evaluator, d.distance);
                else {
                    data.values[d.reg.index()] = (int) ((0.5 + 0.5*CLAMP.d(1-d.distance/255.0, 0, 1))*valueRegion(d.reg, evaluator, d.distance));
                    if (giver == FACTIONS.player() && !DIP.WAR().is(giver, evaluator)) {
                        data.values[d.reg.index()] *= ROPINIONS.STANCE().trustWorthyness(evaluator);
                    }
                }

                offerableWorth += data.values[d.reg.index()];
                tmp.add(rr);


            }
        }



        dirty = true;


    }

    private static boolean log = false;

    private static double valueRegion(Region reg, FactionNPC faction, double dist) {

        if (log)
            LOG.ln(reg.info.name());

        double value = 0;
//		else
//			value *= 0.5 + 0.5*CLAMP.d(1-distance/255.0, 0, 1);

//        for (RDRace r : RD.RACES().all) {
//            value += FSlaves.BASE_PRICE(r.race)*r.pop.get(reg)*0.25;
//        }
//        if (log)
//            LOG.ln("slaves " + value);

        for (RDRace r : RD.RACES().all) {
            // 200 / 20.25 =~ 10 * 5 years =~ 800, *10 for balance
            value += r.pop.target(reg)*800*10;
        }
        if (log)
            LOG.ln("pop " + value);
        double ma = 0;

        for (RDBuilding bu : RD.BUILDINGS().all) {
            if (!bu.isLevelCapped()) {
                continue;
            }

            for (BoostSpec bo : bu.boosters().all()) {
                RESOURCE resource = RD.OUTPUT().fromBoost(bo.boostable);
                if (resource != null) {
                    double multiplier = bu.levels.get(1).local.max(bo.boostable);

                    if (multiplier <= 0) {
                        continue;
                    }

                    ma += bu.levelCap.get(reg) * multiplier * 0.1 * faction.stockpile.price(resource.index(), 1);
                }
            }
        }
        ma *= 16;
        if (log)
            LOG.ln("resources" + " " + ma);
        value += Math.min(ma, value);

        double govPointCost = KingLevels.getInstance().getKingLevel(faction).getGovPointsPerRegion() * 50000;
        if (log)
            LOG.ln("GovPointsPerRegion" + " " + govPointCost);
        value += govPointCost;

        if (log)
            LOG.ln(value);

        if (reg.faction() == faction || RD.OWNER().prevOwner(reg) == faction)
            value *= 2;

        if (reg.faction() == faction && DIP.WAR().is(faction))
            value *= 1.5;

        if (reg.faction() != faction) {
            value *= CLAMP.d(1.0 - dist / 256.0, 0.1, 1);
        }


        return value;

    }

    public static double lootWorth(Region reg) {

        double value = 0;
//		else
//			value *= 0.5 + 0.5*CLAMP.d(1-distance/255.0, 0, 1);

        for (RDRace r : RD.RACES().all) {
            value += FSlaves.BASE_PRICE(r.race)*r.pop.get(reg)*0.25;
        }

        double ma = 0;

        for (RDBuilding bu : RD.BUILDINGS().all) {
            for (BoostSpec bo : bu.boosters().all()) {
                RESOURCE resource = RD.OUTPUT().fromBoost(bo.boostable);
                if (resource != null) {
                    double m = BUtil.value(bu.baseFactors, reg);

                    double v =  bo.booster.max()*m*FACTIONS.PRICE().get(resource);
                    if (v > ma) {
                        ma = v;
                    }

                }
            }
        }
        value += 16*5*ma * (0.05 + RD.RACES().popSizeD(reg));

        return value;
    }

    private void init() {
        for (int i = 0; i < giver.realm().regions(); i++) {
            data.canSelect.set(giver.realm().region(i).index(), false);
        }

        for (RegDist d : WORLD.PATH().regFinder.all(reciever.capitolRegion(), itreaty, sel)){
            data.canSelect.set(d.reg.index(), true);
        }
    }

    private final Treaty itreaty = new Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (to == null)
                return true;
            if (prevReg == null)
                return false;
            if (prevReg.faction() == reciever)
                return true;
            if (prevReg == to)
                return true;
            return to.faction() == giver && data.selected.get(prevReg.index());
        }
    };

    public final class DealReg implements BOOLEAN_MUTABLE{

        private Region reg;

        public Region reg() {
            return reg;
        }

        @Override
        public boolean is() {
            return selected(reg);
        }

        @Override
        public BOOLEAN_MUTABLE set(boolean b) {
            data.selected.set(reg.index(), b);
            dirty = true;
            return this;
        }

        public double value() {
            if (reciever instanceof FactionNPC && reg.faction() == FACTIONS.player() && DIP.WAR().is((FactionNPC)reciever) && !deal.bools.PEACE.is())
                return 0;
            return data.values[reg.index()];
        }

        public boolean canSelect() {
            if (deal.bools.ABSORB.is())
                return false;
            return data.canSelect.get(reg.index());
        }
    }

    public int selfWorth() {
        return selfWorth;
    }

    public int offerableWorth() {
        return offerableWorth;
    }

    public LIST<DealReg> all(){
        if (dirty) {
            init();
            dirty = false;
        }
        return tmp;
    }

    public void clear() {
        init();
    }

    public double worth() {
        double v = 0;
        for (DealReg r : all()) {
            if (r.is())
                v += r.value();
        }
        return v;
    }

    public void add(Region reg) {
        data.selected.set(reg.index(), true);
        dirty = true;
    }

    public void select(Region reg, boolean sel) {
        data.selected.set(reg.index(), sel);
        dirty = true;
    }

    public boolean selected(Region reg) {
        if (deal.bools.ABSORB.is())
            return false;
        return data.selected.get(reg.index());
    }

    public boolean selecteCan(Region reg) {
        return data.canSelect.get(reg.index());
    }

    public int value(Region reg) {
        if (reciever instanceof FactionNPC && reg.faction() == FACTIONS.player() && DIP.WAR().is((FactionNPC)reciever) && !deal.bools.PEACE.is())
            return 0;
        return data.values[reg.index()];
    }

    static class RegData {

        private final Bitmap1D selected = new Bitmap1D(WREGIONS.MAX, false);
        private final Bitmap1D canSelect = new Bitmap1D(WREGIONS.MAX, false);
        private final int[] values = new int[WREGIONS.MAX];

    }

}
