package world.region.building;

import java.util.Arrays;

import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BUtil;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.boosting.Booster;
import game.boosting.BoosterImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.values.GVALUES;
import game.values.Lock;
import init.sprite.UI.UI;
import init.text.D;
import lombok.Getter;
import prplegoo.regions.api.MagicStringChecker;
import settlement.army.div.Div;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.Induvidual;
import snake2d.LOG;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.data.BOOLEANO;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import util.info.INFO;
import util.keymap.MAPPED;
import world.map.regions.Region;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.building.RDBuildPoints.RDBuildPoint;

public final class RDBuilding implements MAPPED{

	public final ArrayListGrower<Booster> baseFactors = new ArrayListGrower<>();
	private final BoostSpecs boosters;
	@Getter
	private final ArrayListGrower<BBoost> bboosts = new ArrayListGrower<>();
	public final Boostable efficiency;
	public final Boostable levelCap;
	public final INT_OE<Region> level;
	public final LIST<RDBuildingLevel> levels;
	public final INFO info;
	private final int index;
	public final RDBuildingCat cat;
	final String kk;
	public final boolean AIBuild;
	public final boolean notify;
	final String order;
	@Getter
	private final RoomBlueprintImp blue;
	private final ArrayList<INT_OE<Faction>> levelAm;

	private static CharSequence ¤¤NotEnough = "Not enough";
	private static CharSequence ¤¤Requirement = "Requirements not met";
	static {
		D.ts(RDBuilding.class);
	}


	RDBuilding(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, String key, INFO info, LIST<RDBuildingLevel> levels, boolean AIBuilds, boolean notify, String order) {
		this(all, init, cat, key, info, levels, AIBuilds, notify, order, null);
	}

	RDBuilding(LISTE<RDBuilding> all, RDInit init, RDBuildingCat cat, String key, INFO info, LIST<RDBuildingLevel> levels, boolean AIBuilds, boolean notify, String order, RoomBlueprintImp blue) {
		this.info = info;
		this.cat = cat;
		this.AIBuild = AIBuilds;
		this.notify = notify;
		this.order = order;
		this.blue = blue;
		cat.all.add(this);
		index = all.add(this);
		kk = cat.key + "_" + key;
		key = "BUILDING_" + kk;
		this.efficiency = BOOSTING.push(key, 1, info.name, info.desc, levels.get(0).icon,  BoostableCat.ALL().WORLD);
		this.levelCap = BOOSTING.push(key + "_LEVEL_CAP", 10, info.name, info.desc, levels.get(0).icon, BoostableCat.ALL().WORLD);



		boosters = new BoostSpecs(info.name, levels.get(0).icon, true);
		RDBuildingLevel flevel = new RDBuildingLevel(Dic.¤¤Clear, UI.icons().m.cancel, GVALUES.REGION.LOCK.push());
		ArrayList<RDBuildingLevel> ll = new ArrayList<>(levels.size() + 1);
		ll.add(flevel);
		ll.add(levels);
		this.levels = ll;
		level = init.count.new DataNibble("BUILDING_LEVEL" + cat.key + " "+ key, ll.size()-1) {
			@Override
			public void set(Region t, int s) {
				if (get(t) != 0 && t.faction() != null)
					levelAm.get(get(t)-1).inc(t.faction(), -1);
				if (s != get(t) && t.faction() == FACTIONS.player())
					RD.BUILDINGS().costs.setDirty();
				super.set(t, s);
				if (get(t) != 0 && t.faction() != null)
					levelAm.get(get(t)-1).inc(t.faction(), 1);
			}
		};

		new RD.RDOwnerChanger() {

			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				int l = level.get(reg);
				if (l > 0) {
					if (oldOwner != null) {
						levelAm.get(l-1).inc(oldOwner, -1);
					}
					if (newOwner != null) {
						levelAm.get(l-1).inc(newOwner, 1);
					}
				}
				if (newOwner == FACTIONS.player() || oldOwner == FACTIONS.player())
					RD.BUILDINGS().costs.setDirty();

			}
		};

		int i = 0;
		for (RDBuildingLevel lll : this.levels) {
			lll.index = i++;
		}

		levelAm = new ArrayList<>(levels.size());
		while(levelAm.hasRoom()) {
			INT_OE<Faction> la = init.rCount.new DataShort("BUILDING_LEVEL" + cat.key + " "+ key + levelAm.size());
			levelAm.add(la);


		}

		values(key);

	}

	private void values(String kk) {
		for (int l = 1; l < levels.size(); l++) {
			final int k = l-1;
			GVALUES.FACTION.pushI(kk + "_LEVEL_" + l, this.levels.get(k).name, this.levels.get(k).icon, levelAm.get(k));
			GVALUES.REGION.pushI(kk + "_KINGDOM_LEVEL_" + l, this.levels.get(k).name, this.levels.get(k).icon, new INT_O<Region>() {

				@Override
				public int get(Region t) {
					if (t.faction() == null)
						return 0;
					return levelAm.get(k).get(t.faction());
				}

				@Override
				public int min(Region t) {
					return 0;
				}

				@Override
				public int max(Region t) {
					return Integer.MAX_VALUE;
				}

			});

		}
		GVALUES.REGION.pushI(kk + "_LEVEL", Dic.¤¤Level + ": "+ info.name, this.levels.get(1).icon, level);

		GVALUES.REGION.push(kk, Dic.¤¤Buildings + ": " + info.names, this.levels.get(1).icon, new BOOLEANO<Region>() {

			@Override
			public boolean is(Region t) {
				return level.get(t) > 0;
			}

		});
		GVALUES.REGION.pushI(kk + "_KINGDOM", Dic.¤¤Buildings + " ("  + Dic.¤¤Realm + "): " + info.names, this.levels.get(1).icon, new INT_O<Region>() {

			@Override
			public int get(Region t) {
				if (t.faction() == null)
					return level.get(t) > 0 ? 1 : 0;
				int am = 0;
				for (INT_OE<Faction> l : levelAm)
					am += l.get(t.faction());
				return am;
			}

			@Override
			public int min(Region t) {
				return 0;
			}

			@Override
			public int max(Region t) {
				return Integer.MAX_VALUE;
			}

		});
		GVALUES.FACTION.pushI(kk + "_AMOUNT", Dic.¤¤Buildings + " ("  + Dic.¤¤Realm + "): " + info.names, this.levels.get(1).icon, new INT_O<Faction>() {

			@Override
			public int get(Faction t) {
				int am = 0;
				for (INT_OE<Faction> l : levelAm)
					am += l.get(t);
				return am;
			}

			@Override
			public int min(Faction t) {
				return 0;
			}

			@Override
			public int max(Faction t) {
				return Integer.MAX_VALUE;
			}

		});

	}

	public SPRITE icon() {
		return levels.get(1).icon;
	}

	public INT_O<Faction> nr(int level){
		return levelAm.get(level);
	}

	protected void connect(RDInit init) {


		KeyMap<BBoost> map = new KeyMap<>();

		boolean[] costs = new boolean[RD.BUILDINGS().costs.all.size()];

		for (RDBuildingLevel l : levels) {

			for (int bi = 0; bi < l.local.all().size(); bi++) {
				BoostSpec lb = l.local.all().get(bi);
				String k = lb.identifier();
				if (!map.containsKey(k)) {
					BBoost b = new BBoost(this, false, lb);
					map.put(k, b);
				}
				RDBuildPoint c = RD.BUILDINGS().costs.get(lb.boostable, lb.booster);

				if (c != null && !costs[c.index]) {

					new Creator.Bo(new BSourceInfo(c.info.name, c.bo.icon), 0, 1, true) {

						@Override
						double get(Region reg) {
							return c.eff(reg);
						};


					}.add(efficiency);
					costs[c.index] = true;
				}
			}

			for (int bi = 0; bi < l.global.all().size(); bi++) {
				BoostSpec lb = l.global.all().get(bi);

				String k = lb.identifier()+"G";
				if (!map.containsKey(k)) {

					BBoost b = new BBoost(this, true, lb);
					map.put(k, b);
				}
			}
		}

		for (RDBuildingLevel l : levels) {

			for (BoostSpec s : l.global.all()) {
				l.local.push(s.booster, s.boostable, Dic.¤¤Realm);
			}
			l.global = null;
		}

		KeyMap<String> lmap = new KeyMap<>();

		for (RDBuildingLevel lev : levels) {

			for (Lock<Region> l : lev.reqs.all()) {
				String k = ""+l.unlocker.name;
				if (!lmap.containsKey(k)) {
					lmap.put(k, k);
					new BoosterImp(new BSourceInfo("!" + l.unlocker.name,  UI.icons().s.boom), 0, 1, true) {
						final int ll = lev.index;
						@Override
						public double vGet(Region t) {
							if (t.faction() == FACTIONS.player()) {
								if (RD.BUILDINGS().tmp().level(RDBuilding.this, t) >= ll)
									return l.unlocker.inUnlocked(t) ? 1 : 0;
							}
							return 1;
						}

					}.add(efficiency);


				}
			}

		}

	}

	public CharSequence canAfford(Region reg, int lc, int level) {

		if (level <= lc)
			return null;
		if (level >= levels.size())
			return Dic.¤¤Unavailable;

		if (blue != null) {
			int levelCap = (int) Math.round(this.levelCap.get(reg));
//			LOG.ln(info.name + " levelcap: " + levelCap);
			if(levelCap < level) {
				return Str.TMP.clear().add(¤¤NotEnough).add(':').s().add(Creator.get¤¤prospect());
			}
		}

		if (reg.faction() != null) {
			int cr = this.levels.get(level).cost - this.levels.get(RD.BUILDINGS().tmp().level(this, reg)).cost;
			if (cr > reg.faction().credits().credits())
				return Str.TMP.clear().add(¤¤NotEnough).add(':').s().add(Dic.¤¤Currs);
		}

		if (this.blue == null) {
			for (int i = lc; i <= level; i++) {
				if (!levels.get(i).reqs.passes(reg))
					return ¤¤Requirement;
			}
		} else if(!this.blue.reqs.passes(reg.faction())) {
			return ¤¤Requirement;
		}

		for (BBoost b : bboosts)
			if (!b.canAfford(reg, lc, level))
				return Str.TMP.clear().add(¤¤NotEnough).add(':').s().add(b.b.boostable.name);
		return null;
	}


	public LIST<RDBuildingLevel> levels(){
		return levels;
	}

	public BoostSpecs boosters(){
		return boosters;
	}

	@Override
	public int index() {
		return index;
	}

	public double baseEfficiency(Region reg) {
		return BUtil.value(baseFactors, reg);
	}

	public static class BBoost extends Booster implements BValue {
		final boolean global;
		final boolean isMul;
		final BoostSpec b;
		final RDBuilding bu;

		public final ArrayListGrower<Booster>[] boosters;



		public BBoost(RDBuilding bu, boolean global, BoostSpec b) {
			super(new BSourceInfo(bu.info.name, global ? Dic.¤¤Realm : null, bu.levels.get(1).icon), b.booster.isMul);
			this.global = global;

			boosters = new ArrayListGrower[bu.levels.size()];
			for(int i = 0; i < boosters.length; i++){
				boosters[i] = new ArrayListGrower<>();
			}

			this.isMul = b.booster.isMul;
			this.bu = bu;
			this.b = b;

			for (int li = 1; li < bu.levels.size(); li++) {
				RDBuildingLevel l = bu.levels.get(li);
				BoostSpecs coll = global ? l.global : l.local;
				for (BoostSpec bb : coll.all()) {
					if (b.isSameAs(bb)) {
						boosters[li].add(bb.booster);
					}
				}
			}

			bu.boosters.push(this, b.boostable, global ? Dic.¤¤Realm : null);
			bu.bboosts.add(this);
		}

		private double tos(int level) {
			double base = isMul ? 1 : 0;
			for(Booster b : boosters[level]){
				if (isMul) {
					base *= b.to();
				} else {
					base += b.to();
				}
			}

			return base;
		}

		private double froms(int level) {
			double base = isMul ? 1 : 0;
			for(Booster b : boosters[level]) {
				if (isMul) {
					base *= b.from();
				} else {
					base += b.from();
				}
			}

			return base;
		}

		private double gs(int level, Region reg) {
			double base = isMul ? 1 : 0;
			for(Booster b : boosters[level]) {
				base += b.get(reg);
			}

			return base;
		}

		@Override
		public double get(BOOSTABLE_O o) {
			return o.boostableValue(this);
		}

		@Override
		public double vGet(Region t) {

			if (global && t.realm() != null) {
				int ll = bu.level.get(t);
				int l = RD.BUILDINGS().tmp().level(bu, t);
				if (ll != l) {
					return vGet(t.faction()) - (tos(ll)-froms(ll)) + (tos(l)-froms(l));
				}
				return vGet(t.faction());

			}
			return g(t);
		}

		@Override
		public double vGet(Faction f) {
			if (f == null)
				return 0;
			double res = 0;
			for (int i = 1; i < boosters.length; i++) {
				double am = bu.levelAm.get(i-1).get(f);
				res += (tos(i)-froms(i))*am;
			}
			if (b.booster.isMul) {
				res += 1;
			}

			return res;
		}

		@Override
		public double vGet(Induvidual indu) {
			return vGet(indu.faction());
		}

		@Override
		public double vGet(Div div) {
			return vGet(div.faction());
		}

		@Override
		public double vGet(PopTime t) {
			return vGet(FACTIONS.player());
		}

		@Override
		public double vGet(Player f) {
			return vGet((Faction)f);
		}

		@Override
		public double vGet(FactionNPC f) {
			return vGet((Faction)f);
		}

		@Override
		public double getValue(double input) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		protected double pget(BOOSTABLE_O o) {
			// TODO Auto-generated method stub
			return 0;
		}

		private double g(Region t) {
			double ta = tos(RD.BUILDINGS().tmp().level(bu, t));
			if (!b.booster.isMul && ta < 0 && !MagicStringChecker.isResourceProductionBooster(b.boostable.key))
				return ta;
			int i = RD.BUILDINGS().tmp().level(bu, t);
			if(MagicStringChecker.isResourceProductionBooster(b.boostable.key)) {
				return bu.efficiency.get(t) * gs(i, t);
			}
			double vv = tos(i);
			if (b.booster.isMul || vv > 0) {
				return froms(i) + bu.efficiency.get(t)*(tos(i)-froms(i));
			}
			return vv;

		}

		@Override
		public double from() {
			return froms(0);
		}

		@Override
		public double to() {
			return tos(boosters.length-1);
		}

		public boolean canAfford(Region reg, int current, int level) {
			if (RD.BUILDINGS().costs.get(b.boostable, b.booster) != null) {
				double am = tos(current) - tos(level);
				if (am <= 0)
					return true;
				return am <= b.boostable.get(reg);
			}

			return true;
		}
	}

	@Override
	public String key() {
		return kk;
	}
}