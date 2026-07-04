package game.raiding;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
import init.sprite.UI.UI;
import init.trade.TR;
import init.trade.TRADABLE;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.iterators.RECedgeIter;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.army.AD;
import world.battle.BattleListener;
import world.entity.army.WArmy;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.map.road.WTRAV;
import world.region.RD;

public class RaidingMap {

    public static CharSequence ¤¤Name = "Raid Security";
    public static CharSequence ¤¤desc = "Rich border regions without proper garrisons or armies might become subject to lesser raiders sacking them. A security above 100% is completely safe.";
    public static CharSequence ¤¤Exposure = "Exposure";
    public static CharSequence ¤¤Protection = "Protection";
    public static CharSequence ¤¤Attraction = "Raider attraction";
    static {
        D.ts(RaidingMap.class);
    }

    private boolean dirty = true;
    private double AUp = -100;
    private final ArrayList<RaidRegion> regsAll = new ArrayList<RaidRegion>(WREGIONS.MAX);
    private final ArrayList<RaidRegion> regsCurrent = new ArrayList<RaidRegion>(WREGIONS.MAX);

    private ArrayListResize<RaidEntryPoint> cooAll = new ArrayListResize<RaidEntryPoint>(128, 1024*16);
    private ArrayListResize<RaidEntryPoint> coosCurrent = new ArrayListResize<RaidEntryPoint>(128, 1024*16);

    private final Bitmap1D rtmp = new Bitmap1D(WREGIONS.MAX+1, false);
    private final RECedgeIter iter = new RECedgeIter();

    RaidingMap() {
        new RD.RDOwnerChanger() {

            @Override
            public void change(Region reg, Faction oldOwner, Faction newOwner) {
                if (oldOwner == FACTIONS.player() || newOwner == FACTIONS.player())
                    dirty = true;
            }
        };

        new DIP.DipActivityListener() {

            @Override
            public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
                if (old == nn)
                    return;
                if (faction == FACTIONS.player() || other == FACTIONS.player())
                    dirty = true;

            }
        };

        new BattleListener() {

            @Override
            public void siege(Faction attacker, Region reg) {
                AUp = -100;

            }

            @Override
            public void siege(WArmy attacker, Region reg) {
                AUp = -100;

            }

            @Override
            public void battle(WArmy a, boolean victory, int losses, int kills, Faction against) {
                AUp = -100;

            }

            @Override
            public void battle(Faction a, boolean victory, int losses, int kills, Faction against) {
                // TODO Auto-generated method stub

            }
        };

        for (int ri = 0; ri < WREGIONS.MAX; ri++) {
            regsAll.add(new RaidRegion(ri));
        }

    }

    public LIST<RaidRegion> entryRegions() {
        process();
        return regsCurrent;
    }

    public LIST<RaidEntryPoint> entrySpots() {
        process();
        return coosCurrent;
    }

    public final MAP_OBJECT<RaidRegion> MAP = new MAP_OBJECT<RaidRegion>() {

        @Override
        public RaidRegion get(int tile) {
            Region reg = WORLD.REGIONS().map.get(tile);
            if (reg != null) {
                process();
                return regsAll.get(reg.index());
            }
            return null;
        }

        @Override
        public RaidRegion get(int tx, int ty) {
            if (WORLD.IN_BOUNDS(tx, ty))
                return get(tx + ty*WORLD.TWIDTH());
            return null;
        }
    };

    public RaidRegion get(Region reg) {
        process();
        return regsAll.get(reg.index());
    }

//	private void process() {
//
//		aProcess();
//
//		if (!dirty)
//			return;
//
//		dirty = false;
//
//
//		Flooder f = GUTIL.flooder();
//
//
//		rtmp.clear();
//		regsCurrent.clearSloppy();
//		coosCurrent.clearSoft();
//
//		{
//
//			f.init(RaidingMap.class);
//
//			iter.init(WORLD.TBOUNDS());
//
//			for(COORDINATE c : WORLD.TBOUNDS()) {
//				if (isPoint(c)) {
//					System.out.println("point! " + c);
//					f.pushSloppy(c, 0);
//					f.setValue2(c, 0);
//				}
//			}
//
//			while(f.hasMore()) {
//
//				PathTile t = f.pollSmallest();
//				Region reg = WORLD.PATH().regMap.get(t);
//
//				if (reg != null && reg.faction() != FACTIONS.player()) {
//					process(t);
//					continue;
//				}
//
//				boolean pushed = false;
//				for (DIR d : DIR.ALL) {
//					if (WORLD.IN_BOUNDS(t, d) && WTRAV.can(t.x(), t.y(), d, false)) {
//						if (f.pushSmaller(t, d, t.getValue()+d.tileDistance(), t) != null) {
//							f.setValue2(t, d, 0);
//							pushed = true;
//						}
//					}
//				}
//
//				if (!pushed)
//					process(t);
//			}
//
//		}
//
//		iter.init(WORLD.TBOUNDS());
//
//		for(COORDINATE c : WORLD.TBOUNDS()) {
//			if (isPoint(c) && f.hasBeenPushed(c) && f.getValue2(c) > 0) {
//
//				while (coosCurrent.size() >= cooAll.size()) {
//					cooAll.add(new RaidEntryPoint());
//				}
//
//				RaidEntryPoint e = cooAll.get(coosCurrent.size());
//				e.coo.set(c);
//
//				e.probability = f.getValue(c);
//
//				if (e.probability <= 0)
//					continue;
//
//				FactionNPC from = passThroughFaction(c);
//
//				e.rFrom = from == null ? -1 : from.index();
//				coosCurrent.add(e);
//
//				Region reg = WORLD.REGIONS().map.get(c);
//				RaidRegion rr = regsAll.get(reg.index());
//				if (!rtmp.get(reg.index())) {
//					regsCurrent.add(rr);
//					rr.points = 0;
//					rr.probability = 0;
//				}
//
//				rr.points++;
//				rr.probability += e.probability;
//
//
//			}
//		}
//
//
//		f.done();
//
//
//
//	}
//
//	private void process(PathTile t) {
//		System.out.println("process! " + t);
//		double v = 0;
//		double dist = 0;
//		while(t.getParent() != null) {
//			Region r = WORLD.REGIONS().map.get(t);
//			if (r == null) {
//				dist += 1.0/32;
//			}
//			else if (r.faction() == null) {
//				v = 1;
//			}else if (r.faction() != FACTIONS.player()) {
//				v = DIP.get((FactionNPC) r.faction()).tarif;
//			}
//			t = t.getParent();
//		}
//		t.setValue2(t.getValue2()+v+dist);
//
//
//	}
//
//	private boolean isPoint(COORDINATE c) {
//
//		Region reg = WORLD.REGIONS().map.get(c);
//		if (reg == null)
//			return false;
//		if (reg.faction() != FACTIONS.player()) {
//			return false;
//		}
//
//		if (!WORLD.PATH().map.is.is(c))
//			return false;
//
//		System.out.println("point!");
//		for (DIR d : DIR.ALL) {
//			if (WORLD.IN_BOUNDS(c, d) && WTRAV.can(c.x(), c.y(), d, false)) {
//				if(WORLD.REGIONS().map.get(c, d) != reg)
//					return true;
//			}
//		}
//		return false;
//	}

    private void process() {

        aProcess();

        if (!dirty)
            return;

        dirty = false;


        Flooder f = GUTIL.flooder();


        rtmp.clear();
        regsCurrent.clearSloppy();
        coosCurrent.clearSoft();

        {
            f.init(RaidingMap.class);

            iter.init(WORLD.TBOUNDS());

            while(iter.hasNext()) {
                COORDINATE c = iter.next();
                f.pushSloppy(c, 0);
            }

            while(f.hasMore()) {

                PathTile t = f.pollSmallest();
                Region reg = WORLD.PATH().regMap.get(t);

                if (reg != null && reg.faction() == FACTIONS.player()) {
                    if (rtmp.get(reg.index()))
                        continue;
                    if (WORLD.PATH().map.is.is(t)) {
                        rtmp.set(reg.index(), true);
                        add(t);
                        continue;
                    }


                }

                if (t.getValue() > 32)
                    break;

                for (DIR d : DIR.ALL) {
                    if (WORLD.IN_BOUNDS(t, d) && WTRAV.can(t.x(), t.y(), d, false)) {
                        f.pushSmaller(t, d, t.getValue()+d.tileDistance());
                    }
                }
            }

            f.done();
        }






        f.init(f);

        for (Region reg : WORLD.REGIONS().active()) {
            RaidRegion r = regsAll.get(reg.index());
            r.probability = 0;
            r.points = 0;
            if (reg.faction() == FACTIONS.player()) {
                continue;
            }
            f.pushSloppy(reg.cx(), reg.cy(), 0);

        }

        while(f.hasMore()) {
            PathTile t = f.pollSmallest();

            Region reg = WORLD.PATH().regMap.get(t);

            if (reg != null && reg.faction() == FACTIONS.player()) {
                rtmp.set(reg.index(), false);
                add(t);
                continue;
            }

            WORLD.PATH().map.pushSimple(t);
        }
        f.done();

        rtmp.clear();



        regsCurrent.clearSloppy();
        for (RaidEntryPoint c : coosCurrent) {
            RaidRegion reg = MAP.get(c.coo);
            if (!rtmp.get(reg.ri)) {
                rtmp.set(reg.ri, true);
                regsCurrent.add(regsAll.get(reg.ri));
            }
            reg.points++;
            reg.probability += c.probability*0.5;
        }

    }



    private void add(PathTile t) {

        PathTile c = t;

        double prob = 0;

        while(t != null) {

            Region reg = WORLD.REGIONS().map.get(t);
            t = t.getParent();
            if (reg == null) {
                prob += 1.0/32.0;
                if (prob >= 1 || t == null) {
                    add(c, 1.0, null);
                    return;
                }
                continue;
            }

            prob = 0;

            if (reg.faction() == null) {
                add(c, 1.0, reg);
                return;
            }

            if (reg.faction() == FACTIONS.player()) {
                if (t == null) {
                    add(c, 1.0, null);
                    return;
                }
                continue;
            }

            add(c, DIP.get((FactionNPC) reg.faction()).tarif, reg);
            return;
        }

    }

    public static FactionNPC passThroughFaction(COORDINATE c) {
        Faction fa = null;
        for (DIR d : DIR.ALL) {

            if (WORLD.PATH().map.can(c, d)) {
                Faction f = WORLD.REGIONS().faction.get(c, d);
                if (f == null)
                    return null;
                if (f != FACTIONS.player() && DIP.get(FACTIONS.player(), f).trades) {
                    fa = f;
                }
            }
        }
        if (fa == null)
            return null;
        return (FactionNPC) fa;
    }





    private void add(COORDINATE c, double probability, Region from) {

        while (coosCurrent.size() >= cooAll.size()) {
            cooAll.add(new RaidEntryPoint());
        }

        RaidEntryPoint e = cooAll.get(coosCurrent.size());
        e.coo.set(c);

        e.probability = probability;
        e.rFrom = from == null ? -1 : from.index();
        coosCurrent.add(e);

    }

    private void aProcess() {
        if (Math.abs(AUp - TIME.currentSecond()) < 100)
            return;

        AUp = TIME.currentSecond();

        for (RaidRegion r : regsAll)
            r.armyPower = 0;

        for (int ai = 0; ai < FACTIONS.player().armies().all().size(); ai++) {
            WArmy a = FACTIONS.player().armies().all().get(ai);
            double pow = AD.power().get(a);
            if (pow <= 0)
                continue;
            for (RegDist d : WORLD.PATH().regFinder.all(a.ctx(), a.cty(), aTreaty, WRegSel.DUMMY())) {
                double p = 1.0 - (d.distance - 32)/96.0;
                p = CLAMP.d(p, 0, 1);
                get(d.reg).armyPower += pow*p;
            }

        }
    }

    private final Treaty aTreaty = new Treaty() {

        @Override
        public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
            if (dist > 128)
                return false;
            return true;
        }

    };

    public static class RaidRegion {

        public final int ri;
        private double probability;
        private double armyPower;
        private int points = 0;

        private int spoils = 0;
        private int spoilsI = 0;


        private RaidRegion(int reg) {
            this.ri = reg;
        }

//		public double probability() {
//			double w = spoils()*probability;
//			double pow = (armyPower + RD.MILITARY().power.getD(r()));
//
//			double res = w/(pow+1);
//			return res;
//		}

        public double security() {
            double w = spoils()*probability;
            double pow = (armyPower + RD.MILITARY().power.getD(r()));
            return CLAMP.d((pow+1)/w, 0, 10);
        }

        public double probabilityRaw() {
            return probability;
        }

        public double army() {
            return armyPower;
        }

        public Region r() {
            return WORLD.REGIONS().all().get(ri);
        }

        public int points() {
            return points;
        }



        public double spoils() {
            if (spoilsI == GAME.updateI())
                return spoils;

            spoils = 0;

            for (TRADABLE tr : TR.ALL()) {
                spoils += Math.max(0.0, FACTIONS.PRICE().get(tr)*RD.OUTPUT().get(tr).getDelivery(r())/(RD.DIST().distancePenalty(r())));
            }
            spoils += RD.OUTPUT().MONEY.getDelivery(r())/(RD.DIST().distancePenalty(r()));

            spoils /= NPCStockpile.AVERAGE_PRICE;

            spoilsI = GAME.updateI();

            return spoils;
        }

        public void hoverInfoGet(GUI_BOX box) {

            GBox b = (GBox) box;

            b.title(RaidingMap.¤¤Name);
            b.text(RaidingMap.¤¤desc);
            b.NL();

            b.add(UI.icons().m.raider);
            b.textLL(¤¤Attraction);
            b.tab(7);
            b.add(GFORMAT.i(b.text(), (int)(spoils()*probabilityRaw())));
            b.NL();

            b.tab(1);
            b.add(UI.icons().s.money);
            b.textLL(Dic.¤¤Riches);
            b.tab(7);
            b.add(GFORMAT.i(b.text(), (int)spoils()));
            b.NL();

            b.tab(1);
            b.add(UI.icons().s.minimap);
            b.textLL(RaidingMap.¤¤Exposure);
            b.tab(7);
            b.add(GFORMAT.mul(b.text(), probabilityRaw()));
            b.NL();

            b.NL(16);

            b.add(UI.icons().s.shield);
            b.textLL(¤¤Protection);
            b.tab(7);
            b.add(GFORMAT.iIncr(b.text(), 1+(int)RD.MILITARY().power.getD(r()) + (int)army()));
            b.NL();

            b.tab(1);
            b.textL(Dic.¤¤Garrison);
            b.tab(7);
            b.add(GFORMAT.i(b.text(), (int)RD.MILITARY().power.getD(r())));
            b.NL();

            b.tab(1);
            b.textL(Dic.¤¤Armies);
            b.tab(7);
            b.add(GFORMAT.i(b.text(), (int)army()));
            b.NL();

            b.sep();

            b.textLL(Dic.¤¤Total);
            b.tab(6);

            GText t = b.text();
            t.add(1+(int)RD.MILITARY().power.getD(r()) + (int)army()).s().add('/').s().add((int)(spoils()*probabilityRaw())).s().add('=').s().add(security());
            b.add(t);

        };
    }

    public static class RaidEntryPoint {

        private double probability = 0;
        private final Coo coo = new Coo();
        private int rFrom = -1;

        public double probabilityRaw() {
            return probability;
        }

        public COORDINATE c() {
            return coo;
        }

        public Region from() {
            if (rFrom == -1)
                return null;
            return WORLD.REGIONS().all().get(rFrom);
        }

    }





}
