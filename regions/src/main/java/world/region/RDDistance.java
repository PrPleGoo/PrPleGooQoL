package world.region;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.values.GVALUES;
import init.sprite.UI.UI;
import init.text.D;
import prplegoo.regions.api.npc.KingLevels;
import snake2d.PathTile;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.BOOLEANO;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import world.WORLD;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.RDOutputs.RDOutput;
import world.region.pop.RDRace;

public class RDDistance {

	private static CharSequence ¤¤Name = "¤Proximity";
	private static CharSequence ¤¤NameD = "¤Proximity is the physical distance from a region to your capital. It determines the amount tribute you receive from it and the loyalty of its subjects. It also determines tolls for trade.";

	private static CharSequence ¤¤Distance = "¤Distance";
	private static CharSequence ¤¤DistanceD = "¤Distance to your capital. Distance affect trade prices.";

	private static CharSequence ¤¤Borders = "¤Borders";
	private static CharSequence ¤¤Reachable = "¤Reachable";

	private final INT_OE<Region> distance;
	private final INT_OE<Faction> factionBorders;
	private final INT_OE<Faction> factionReachable;
	private final INT_OE<Faction> factionBorderThroughAlly;
	private final INT_OE<Region> regionBorders;
	private final INT_OE<Region> regionReachable;
	public final Boostable boostable;

	private final ArrayList<FactionNPC> borders = new ArrayList<>(FACTIONS.MAX());
	private boolean bDirty = true;

	private int[] dists = new int[FACTIONS.MAX()];

	static {
		D.ts(RDDistance.class);
	}

	RDDistance(RDInit init) {
		distance = init.count.new DataShort("DISTANCE_DATA", ¤¤Distance, ¤¤DistanceD);
		factionReachable = init.rCount.new DataBit("DISTANCE_REACHABLE");
		factionBorders = init.rCount.new DataBit("DISTANCE_NEIGHBOURS");
		factionBorderThroughAlly = init.rCount.new DataBit("DISTANCE_NEIGHBOURS:ALLY");
		regionReachable = init.count.new DataBit("REGION_REACHABLE");
		regionBorders = init.count.new DataBit("REGION_NEIGHBOURS");
		Arrays.fill(dists, -1);
		boostable = BOOSTING.push("PROXIMITY", 1, ¤¤Name, ¤¤NameD, UI.icons().s.wheel,  BoostableCat.ALL().WORLD);

		new RBooster(new BSourceInfo(Dic.¤¤Distance, UI.icons().s.wheel), 1, 0.01, true) {

			final double II = 1.0/1024;

			@Override
			public double get(Region t) {
				return CLAMP.d((distance.get(t)-48)*II, 0, 1);
			}

		}.add(boostable);

		BOOSTING.connecter(new ACTION() {

			@Override
			public void exe() {
				RBooster bo = new RBooster(new BSourceInfo(Dic.¤¤Distance, UI.icons().s.wheel), 0.1, 1, true) {
					@Override
					public double get(Region t) {
						if (t.faction() != FACTIONS.player())
							// TODO: fix distance for Npcs
							return KingLevels.isActive() ? 1 : 0;
						return CLAMP.d(boostable.get(t), 0, 1);
					}


				};
				for (RDRace r : RD.RACES().all) {
					bo.add(r.loyalty.target);
				}
				for (RDOutput o : RD.OUTPUT().ALL)
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

		new RD.RDOwnerChanger() {

			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				bDirty = true;
			}
		};

		new DIP.DipActivityListener() {

			@Override
			public void change(Faction faction, Faction other, DipStance old, DipStance nn) {
				bDirty = true;

			}
		};

	}

	private void init() {

		if (!bDirty)
			return;

		Region cap = FACTIONS.player().capitolRegion();
		if (cap == null)
			return;

		WORLD.FOW().setDirty();
		bDirty = false;

		for (Region reg : WORLD.REGIONS().all()) {
			regionReachable.set(reg, 0);
			regionBorders.set(reg, 0);
			distance.setD(reg, 0);
		}
		for (Faction f : FACTIONS.all()) {
			factionReachable.set(f, 0);
			factionBorders.set(f, 0);
			factionBorderThroughAlly.set(f, 0);
		}

		for (RegDist d : WORLD.PATH().regFinder.all(cap, Treaty.DUMMY, WRegSel.DUMMY())) {
			distance.set(d.reg, CLAMP.i(d.distance, 0, distance.max(null)));
		}

		borders.clearSloppy();
		for (RegDist d : WORLD.PATH().regFinder.all(cap, Treaty.FACTION_REACHABLE, WRegSel.DUMMY())) {
			distance.set(d.reg, CLAMP.i(d.distance, 0, distance.max(null)));
			regionReachable.set(d.reg, 1);
			if (d.reg.faction() != null) {
				if (d.reg.capitol()) {
					factionReachable.set(d.reg.faction(), 1);
					if (d.reg.faction() != FACTIONS.player())
						borders.add((FactionNPC) d.reg.faction());
				}
			}
		}
		for (RegDist d : WORLD.PATH().regFinder.all(cap, Treaty.FACTION_CAN_ATTACK, WRegSel.DUMMY())) {
			if (d.reg.faction() != null) {
				if (d.reg.capitol()) {
					factionBorderThroughAlly.set(d.reg.faction(), 1);
				}
			}
		}

		for (RegDist d : WORLD.PATH().regFinder.all(cap, Treaty.FACTION_BORDERS, WRegSel.DUMMY())) {

			regionBorders.set(d.reg, 1);
			if (d.reg.faction() != null) {
				factionBorders.set(d.reg.faction(), 1);

			}
		}
	}

	public int distance(Faction f) {
		init();
		return distance.get(f.capitolRegion());
	}

	public final INT_O<Region> distance(){
		init();
		return distance;
	}

	public boolean reachable(Region reg) {
		init();
		return regionReachable.get(reg) == 1;
	}

	public boolean neighbours(Region reg) {
		init();
		return regionBorders.get(reg) == 1;
	}

	public boolean reachable(Faction reg) {
		init();
		return factionReachable.get(reg) == 1;
	}

	public boolean factionHasRegionBorderingPlayer(Faction reg) {
		init();
		return factionBorders.get(reg) == 1;
	}

	public boolean factionCanAttackPlayerAllies(Faction reg) {
		init();
		return factionBorderThroughAlly.get(reg) == 1;
	}

	public LIST<RegDist> tradePartners(Faction start) {

		selTradeF = start;

		if (start instanceof FactionNPC) {
			return WORLD.PATH().regFinder.all(start.capitolRegion(), KingLevels.isActive() ? Treaty.FACTION_BORDERS : Treaty.FACTION_REACHABLE_NPC_TRADE, selTrade);
		}

		return WORLD.PATH().regFinder.all(start.capitolRegion(), Treaty.FACTION_REACHABLE, selTrade);
	}

	public LIST<FactionNPC> neighs(){
		init();
		return borders;
	}

	public int capitolDist(FactionNPC f) {
		if (dists[f.index()] == -1) {
			PathTile d =  WORLD.PATH().path(FACTIONS.player().cx(), FACTIONS.player().cy(), f.cx(), f.cy(), Treaty.DUMMY);
			if (d == null)
				dists[f.index()] = 0;
			else {
				dists[f.index()] = (int) d.getValue();
			}
		}
		return dists[f.index()];
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


}
