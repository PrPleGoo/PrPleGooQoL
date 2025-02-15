package world.army;

import game.boosting.BSourceInfo;
import game.boosting.BoosterAbs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.sprite.UI.UI;
import init.text.D;
import prplegoo.regions.api.npc.KingLevels;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.*;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import view.ui.message.MessageText;
import world.army.ADInit.Register;
import world.army.ADInit.Updater;
import world.army.ADInt.ADIntImp;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;

public final class ADSupplies {

	public final static int artilleryMax = 40;

	private final ArrayList<ArrayListGrower<ADSupply>> map = new ArrayList<ArrayListGrower<ADSupply>>(RESOURCES.ALL().size());
	private final ArrayListGrower<RESOURCE> resources = new ArrayListGrower<RESOURCE>();
	{
		while(map.hasRoom())
			map.add(new ArrayListGrower<ADSupply>());

	}
	final ADIntImp creditsNeeded;
	final ADIntImp creditsTarget;
	public final LIST<ADSupply> all;
	public final LIST<ADSupply> healths;
	public final LIST<ADSupply> morales;
	public final LIST<ADSupply> food;
	public final LIST<ADSupply> equip;

	private final Bitmap1D has = new Bitmap1D(RESOURCES.ALL().size(), false);
	private final ArrayListGrower<ADArtillery> arts = new ArrayListGrower<>();
	private final INT_OE<WArmy> artilleryTot;

	private static CharSequence ¤¤Starving = "¤Supplies low!";
	private static CharSequence ¤¤StarvingD = "¤Essential supplies have not been delivered to our army, affecting health. Low health will stop training of new recruits and cause deaths and desertion. Fill up our military depots and fortify the army immediately. Affected army: {0}.";

	static {
		D.ts(ADSupplies.class);
	}

	ADSupplies(ADInit init){
		creditsNeeded = new ADIntImp(init, "CREDITS_CURRENT", Dic.¤¤Currs, "");
		creditsTarget = new ADIntImp(init, "CREDITS_TARGET", Dic.¤¤Currs, "");
		ArrayListGrower<ADSupply> all = new ArrayListGrower<>();

		ArrayListGrower<ADSupply> misc = new ArrayListGrower<>();
		for (ResSupply a : RESOURCES.SUP().ALL) {
			ADSupply s = new ADSupply(all.size(), "SUPPLY", init, a.resource, Dic.¤¤Supplies, a.consumption_day, a.morale, a.health);
			all.add(s);
			misc.add(s);
		}
		this.food = misc;

		ArrayListGrower<ADSupply> equip = new ArrayListGrower<>();
		for (EquipBattle a : STATS.EQUIP().BATTLE_ALL()) {
			ADSupply s = new ADSupply(all.size(), "EQUIPMENT", init, a.resource(), Dic.¤¤Equipment, a.wearRate()/16.0, 0, 0);
			all.add(s);
			equip.add(s);
		}
		this.equip = equip;

		init.registers.add(new Register() {

			@Override
			public void register(ADDiv div, int d) {
				if (div.needSupplies()) {
					for(int i = 0; i < RESOURCES.SUP().ALL.size(); i++) {
						ResSupply s = RESOURCES.SUP().ALL.get(i);

						get(s).needed.inc(div.army(), d*s.minimum(div.race())*div.men());
						get(s).target.inc(div.army(), d*s.minimum(div.race())*div.menTarget());
					}
					for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++ ) {
						EquipBattle s = STATS.EQUIP().BATTLE_ALL().get(i);
						get(s).needed.inc(div.army(), d*div.men()*div.equipTarget(s));
						get(s).target.inc(div.army(), d*div.menTarget()*div.equipTarget(s));
					}
				}

				AD.supplies().creditsNeeded.inc(div.army(), d*div.costPerMan()*div.men());
				AD.supplies().creditsTarget.inc(div.army(), d*div.costPerMan()*div.menTarget());
			}
		});

		artilleryTot = init.dataA.new DataInt("ARTILLARY_TARGET_TOT");

		for (ROOM_ARTILLERY a : SETT.ROOMS().ARTILLERY)
			arts.add(new ADArtillery(init, a, all));


		this.all = all;
		{
			ArrayListGrower<ADSupply> mm = new ArrayListGrower<>();
			ArrayListGrower<ADSupply> he = new ArrayListGrower<>();
			for (ADSupply s : all) {
				if (s.morale > 0) {
					mm.add(s);
				}
				if (s.health > 0)
					he.add(s);
			}
			this.healths = he;
			this.morales = mm;
		}

		AD.moraleFactors().add(new BoosterAbs<WArmy>(new BSourceInfo(Dic.¤¤Supplies, UI.icons().s.storage), false) {

			@Override
			public double to() {
				return 1;
			}

			@Override
			protected double pget(WArmy o) {
				return morale(o);
			}

			@Override
			public double from() {
				return 0;
			}

			@Override
			public double getValue(double input) {
				return input;
			}
		});

		init.updaters.add(new Updater() {

			@Override
			public void update(Faction f, double timeSinceLast) {
				// TODO Auto-generated method stub

			}

			@Override
			public void update(WArmy a, double timeSinceLast) {

				if (KingLevels.isActive() || a.faction() == FACTIONS.player()) {
					double he = health(a);
					for (ADSupply s : all) {
						double am = s.usedPerDay*s.needed.get(a);
						int tot = (int) am;
						if (am-tot > RND.rFloat())
							tot++;
						s.current.inc(a, -tot);
					}

					if (a.faction() == FACTIONS.player() && he >= 1 && AD.supplies().health(a) < 1) {
						Str.TMP.clear();
						Str.TMP.add(¤¤StarvingD).insert(0, a.name);
						new MessageText(¤¤Starving, Str.TMP).send();
					}
				}else if (a.acceptsSupplies()){
					for (ADSupply s : AD.supplies().all) {
						double am = Math.ceil(s.used().get(a)/16.0);
						am = CLAMP.d(am, 0, s.needed(a));
						s.current().inc(a, (int)am);
					}
				}

			}
		});

		Bitmap1D res = new Bitmap1D(RESOURCES.ALL().size(), false);
		for (ADSupply a : this.all) {
			has.set(a.res.index(), true);
			map.get(a.res.index()).add(a);
			if (!res.get(a.res.index())) {
				resources.add(a.res);
				res.set(a.res.index(), true);
			}
		}

	}

	public LIST<ADArtillery> arts(){
		return arts;
	}

	public LIST<RESOURCE> resses(){
		return resources;
	}

	public LIST<ADSupply> get(RESOURCE res) {
		return map.get(res.index());
	}

	public ADSupply get(ResSupply res) {
		return all.get(res.index());
	}

	public ADSupply get(EquipBattle a) {
		return all.get(RESOURCES.SUP().ALL.size() + a.indexMilitary());
	}

	public void fillAll(WArmy a) {
		for (ADSupply s : all) {
			s.current.set(a, s.max(a));
		}
	}

	public void update(WArmy a) {
		for (ADSupply s : all) {
			double am = s.usedPerDay*s.needed.get(a);
			int tot = (int) am;
			if (am-tot > RND.rFloat())
				tot++;
			s.current.inc(a, -tot);
		}

	}

	public ADInt credits() {
		return creditsNeeded;
	}

	public double morale(WArmy a) {
		double m = 0;
		for (ADSupply s : morales) {
			double u = s.used().get(a);
			if (u > 0)
				m += s.morale*CLAMP.d(s.current().get(a)/u, 0, 1);
		}
		return m;
	}

	public double health(WArmy a) {
		double m = 1;
		for (ADSupply s : healths) {
			if (s.current.get(a) < s.used().get(a)) {
				m *= 1 - s.health * (s.used().get(a) - s.current().get(a))/s.used().get(a);

			}
		}
		return m;
	}

	public void transfer(WDIV div, WArmy old, WArmy current) {
		if (old == null || current == null)
			return;
		for(ResSupply s : RESOURCES.SUP().ALL) {
			double tot = get(s).max(old) + get(s).max(div.menTarget()*s.minimum(div.race()));
			if (tot > 0) {
				double d = CLAMP.d(get(s).current().get(old)/tot, 0, 1);
				int am = get(s).max((int)(div.menTarget()*d*s.minimum(div.race())));
				get(s).current().inc(old, -am);
				get(s).current().inc(current, am);
			}
		}
		for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
			double tot = get(s).max(old) +  get(s).max(div.menTarget()*div.equipTarget(s)) ;
			if (tot > 0) {
				double d = CLAMP.d(get(s).current().get(old)/tot, 0, 1);
				int am = get(s).max((int)(div.menTarget()*d*div.equipTarget(s)));
				get(s).current().inc(old, -am);
				get(s).current().inc(current, am);
			}
		}
	}


	public final class ADArtillery implements INDEXED{

		public final ROOM_ARTILLERY art;
		public final INT_OE<WArmy> target;
		private final ArrayListGrower<ADSupply> supplies = new ArrayListGrower<>();

		ADArtillery(ADInit init, ROOM_ARTILLERY art, LISTE<ADSupply> sups){
			this.art = art;
			target = init .dataA.new DataByte("ART_TARGET_" + art.key) {

				@Override
				public void set(WArmy t, int s) {

					artilleryTot.inc(t, -get(t));
					super.set(t, s);
					artilleryTot.inc(t, get(t));
					int k = 0;
					for (ADSupply ss : supplies) {
						int am = (int) Math.ceil(get(t)*art.constructor().item(1).cost2(k++, art.upgrades().max()));
						ss.needed.set(t, am);
						ss.target.set(t, am);
					}
				}

				@Override
				public int max(WArmy t) {
					return get(t)+artilleryMax-artilleryTot.get(t);
				}

			};

			for (int i = 0; i < art.constructor().resources(); i++) {
				RESOURCE res = art.constructor().resource(i);
				ADSupply sup = new ADSupply(sups.size(), "ART_" + art.key + "_" + res.key, init, res, art.info.name + ": ", 0.2/TIME.years().bitConversion(TIME.days()), 0, 0);
				supplies.add(sup);
				sups.add(sup);
			}

		}

		public int current(WArmy a) {
			double d = 1.0;
			for (ADSupply ss : supplies) {
				d = Math.min(d, ss.equipValue(a));
			}
			if (a.state() != WArmyState.fortified )
				d *= 0.5;
			return (int) (target.get(a)*d);
		}

		@Override
		public int index() {
			return art.typeIndex();
		}

		public LIST<ADSupply> sups(){
			return supplies;
		}

	}



}