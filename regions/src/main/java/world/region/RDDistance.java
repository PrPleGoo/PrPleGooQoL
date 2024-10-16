package world.region;

import java.io.IOException;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import init.sprite.UI.UI;
import init.text.D;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import world.WORLD;
import world.map.pathing.WPATHING;
import world.map.pathing.WRegSel;
import world.map.pathing.WRegs.RDist;
import world.map.pathing.WTREATY;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RDOutput.RDResource;
import world.region.pop.RDRace;

public class RDDistance {

    private static CharSequence ¤¤Name = "¤Proximity";
    private static CharSequence ¤¤NameD = "¤Proximity is the physical distance from a region to your capital. It determines the amount tribute you receive from it and the loyalty of its subjects.";

    private static CharSequence ¤¤Distance = "¤Distance";
    private static CharSequence ¤¤DistanceD = "¤Distance to your capital. Distance affect trade prices.";


    private final INT_OE<Region> data;
    private final INT_OE<Faction> factionNeighbours;
    private final INT_OE<Faction> factionReachable;
    private final INT_OE<Region> regionNeighbours;
    private final INT_OE<Region> regionReachable;
    public final Boostable boostable;

    private final ArrayList<FactionNPC> borders = new ArrayList<>(FACTIONS.MAX);
    private boolean bDirty = true;

    static {
        D.ts(RDDistance.class);
    }

    RDDistance(RDInit init) {
        data = init.count.new DataShort("DISTANCE_DATA", ¤¤Distance, ¤¤DistanceD);
        factionReachable = init.rCount.new DataBit("DISTANCE_REACHABLE");
        factionNeighbours = init.rCount.new DataBit("DISTANCE_NEIGHBOURS");
        regionReachable = init.count.new DataBit("REGION_REACHABLE");
        regionNeighbours = init.count.new DataBit("REGION_NEIGHBOURS");

        if (false)
            ; //unreachable regions must be unreachable in the UI and not produce taxes. Also factions should be able to attack you when they border one of your regions.

        boostable = BOOSTING.push("PROXIMITY", 1, ¤¤Name, ¤¤NameD, UI.icons().s.wheel, BoostableCat.WORLD);

        new RBooster(new BSourceInfo(Dic.¤¤Distance, UI.icons().s.wheel), 1, 0.01, true) {

            final double II = 1.0/(256+128);

            @Override
            public double get(Region t) {
                return CLAMP.d((WORLD.PATH().distance(t, t.faction().capitolRegion()) - 48.0) * II, 0, 1);
            }

        }.add(boostable);

        BOOSTING.connecter(new ACTION() {

            @Override
            public void exe() {
                RBooster bo = new RBooster(new BSourceInfo(boostable.name, UI.icons().s.wheel), 0.01, 1, true) {
                    @Override
                    public double get(Region t) {
                        if (t.faction() != FACTIONS.player())
                            return 1;
                        return CLAMP.d(boostable.get(t), 0, 1);
                    }


                };
                for (RDRace r : RD.RACES().all) {
                    bo.add(r.loyalty.target);
                }
                for (RDResource o : RD.OUTPUT().all)
                    bo.add(o.boost);
            }
        });

        init.savable.add(new SAVABLE() {

            @Override
            public void save(FilePutter file) {
                // TODO Auto-generated method stub

            }

            @Override
            public void load(FileGetter file) throws IOException {
                bDirty = true;
            }

            @Override
            public void clear() {
                bDirty = true;
            }
        });
    }

    void init() {

        if (false) {
            //I suspect that factions that reach their regions trhough the sea, will not get reported as faction Reachable, but get the factionNeighbours. This is odd, and wrong, as you can trade with these.
        }

        bDirty = true;
        Region cap = FACTIONS.player().capitolRegion();
        if (cap == null)
            return;
        for (Region reg : WORLD.REGIONS().all()) {
            regionReachable.set(reg, 0);
            regionNeighbours.set(reg, 0);
            data.setD(reg, 0);
        }
        for (Faction f : FACTIONS.all()) {
            factionReachable.set(f, 0);
            factionNeighbours.set(f, 0);
        }

        for (int ri = 0; ri < FACTIONS.player().realm().regions(); ri++) {
            Region reg = FACTIONS.player().realm().region(ri);
            regionReachable.set(reg, 1);
            for (RDist d : WORLD.PATH().tmpRegs.all(reg, WTREATY.NEIGHBOURS(reg), WRegSel.DUMMY())) {
                regionNeighbours.set(d.reg, 1);
                if (d.reg.faction() != null)
                    factionNeighbours.set(d.reg.faction(), 1);
            }
        }

        for (RDist d : neighs(FACTIONS.player(), WRegSel.CAPITOLS(FACTIONS.player()))) {
            regionReachable.set(d.reg, 1);
            data.set(d.reg, CLAMP.i(d.distance, 0, data.max(null)));
            if (d.reg.faction() != null && d.reg.faction() != FACTIONS.player()) {
                factionReachable.set(d.reg.faction(), 1);
            }
        }

        for (RDist d : WORLD.PATH().tmpRegs.all(cap, WTREATY.DUMMY(), WRegSel.DUMMY())) {
            if (regionReachable.get(d.reg) == 0)
                data.set(d.reg, CLAMP.i(d.distance, 0, data.max(null)));
        }


    }

    public int distance(Faction f) {
        return data.get(f.capitolRegion());
    }

    public final INT_O<Region> distance(){
        return data;
    }

    public boolean reachable(Region reg) {
        return regionReachable.get(reg) == 1;
    }

    public boolean neighbours(Region reg) {
        return regionNeighbours.get(reg) == 1;
    }

    public boolean reachable(Faction reg) {
        return factionReachable.get(reg) == 1;
    }

    public boolean neighbours(Faction reg) {
        return factionNeighbours.get(reg) == 1;
    }

//	public boolean factionIsAlmostReachable(Faction f) {
//		return factionReachable.get(f) == 1;
//	}
//
//	public boolean factionTradable(Faction f) {
//		return factionReachable.get(f) == 1;
//	}
//
//	public boolean factionBordersPlayer(Faction f) {
//		for (int ri = 0; ri < f.realm().regions(); ri++) {
//			if (regionReachable.get(f.realm().region(ri)) == 1)
//				return true;
//		}
//		return false;
//	}
//
//	public boolean regionBordersPlayer(Region reg) {
//		return regionReachable.get(reg) == 1;
//	}

    private final Bitmap1D check = new Bitmap1D(FACTIONS.MAX, false);

    private final WTREATY treaty = new WTREATY() {

        @Override
        public boolean can(int fx, int fy, int tx, int ty, double dist) {
            Region from = WORLD.REGIONS().map.get(fx, fy);

            if (from == null)
                return true;

            if (from.faction() == null)
                return false;

            return check.get(from.faction().index());
        }
    };

    public LIST<RDist> neighs(Faction start, WRegSel sel) {

        check.clear();
        check.set(start.index(), true);
        for (RDist d : WORLD.PATH().tmpRegs.all(start.capitolRegion(), WTREATY.NEIGHBOURSF(start), WRegSel.DUMMY())){
            if (d.reg.faction() != null)
                check.set(d.reg.faction().index(), true);
        }

        return WORLD.PATH().tmpRegs.all(start.capitolRegion(), treaty, sel);

    }

    private Faction selTradeF;

    private final WRegSel selTrade = new WRegSel() {

        @Override
        public boolean is(Region t) {

            if (t.faction() == null)
                return false;
            if (!t.capitol())
                return false;
            if (t.faction() == selTradeF)
                return false;
            if (DIP.get(selTradeF, t.faction()).trades)
                return true;
            if (selTradeF instanceof FactionNPC && t.faction() instanceof FactionNPC)
                return DIP.get(selTradeF, t.faction()) == DIP.NEUTRAL();
            return false;
        }
    };

    public LIST<RDist> tradePartners(Faction start) {

        selTradeF = start;
        return neighs(start, selTrade);
    }


    public LIST<FactionNPC> neighs(){
        if (bDirty) {
            bDirty = false;
            borders.clearSloppy();


            for (FactionNPC f : FACTIONS.NPCs()) {
                if (RD.DIST().reachable(f)) {
                    borders.add(f);
                }
            }
        }
        return borders;
    }


}
