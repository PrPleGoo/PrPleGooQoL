package world.army;

import game.boosting.BSourceInfo;
import game.boosting.BoosterAbs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResSupply;
import init.sprite.UI.UI;
import prplegoo.regions.api.npc.KingLevels;
import settlement.main.SETT;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.data.INT_O.INT_OE;
import util.text.D;
import util.text.Dic;
import view.ui.message.MessageText;
import world.army.ADInit.Register;
import world.army.ADInit.Updater;
import world.army.ADInt.ADIntImp;
import world.army.ADSupply.ADSupplyArt;
import world.army.ADSupply.ADSupplyRes;
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
	public final LIST<ADSupply.ADSupplyRes> food;
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

		ArrayListGrower<ADSupplyRes> misc = new ArrayListGrower<>();
		for (ResSupply rs : RESOURCES.SUP().ALL) {
			ADSupplyRes s = new ADSupply.ADSupplyRes(all.size(), init, rs);
			all.add(s);
			misc.add(s);
		}
		this.food = misc;

		ArrayListGrower<ADSupply> equip = new ArrayListGrower<>();
		for (EquipBattle a : STATS.EQUIP().BATTLE_ALL()) {
			ADSupply s = new ADSupply.ADSupplyEquip(all.size(), init, a);
			all.add(s);
			equip.add(s);
		}
		this.equip = equip;

		init.registers.add(new Register() {

			@Override
			public void register(ADDiv div, int d) {
				if (div.army() != null) {
					for (int si = 0; si < all.size(); si++) {
						all.get(si).setChanged(div.army());
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
				if (s.baseMorale > 0) {
					mm.add(s);
				}
				if (s.baseHealth > 0)
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
						double am = s.consumedPerDayCurrent(a)*timeSinceLast*TIME.secondsPerDayI();
						int tot = (int) am;
						if (am-tot > RND.rFloat())
							tot++;
						s.current().inc(a, -tot);
					}

					if (a.faction() == FACTIONS.player() && he >= 1 && AD.supplies().health(a) < 1) {
						Str.TMP.clear();
						Str.TMP.add(¤¤StarvingD).insert(0, a.name);
						new MessageText(¤¤Starving, Str.TMP).send();
					}
				}else {
					for (ADSupply s : AD.supplies().all) {
						double am = Math.ceil(s.targetAmount(a)/16.0);
						am = CLAMP.d(am, 0, s.minimumAmount(a));
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
			s.current().set(a, s.targetAmount(a));
		}
	}

	public ADInt credits() {
		return creditsNeeded;
	}

	public double morale(WArmy a) {
		double m = 0;
		for (ADSupply s : morales) {
			m += s.moraleAdd(a);
		}
		return m;
	}

	public double health(WArmy a) {
		double m = 1;
		for (ADSupply s : healths) {
			m *= s.healthMul(a);
		}
		return m;
	}

	public double supplyEquip(WArmy a) {
		double current = 0.0;
		double needed = 0.0;
		for (ADSupply s : healths) {
			current += s.current.get(a);
			needed += s.targetAmount(a);
		}

		if (needed == 0) {
			return 1;
		}
		return current / needed;
	}

	public boolean isMissingArtsEquip(ADArtillery arts, WArmy a) {
		for (ADSupply s : arts.supplies) {
			double needed = s.targetAmount(a);
			if (needed == 0) {
				continue;
			}

			double current = s.current.get(a);
			if (current / needed < 0.75) {
				return true;
			}
		}

		return false;
	}

	public double equip(WArmy a) {
		double current = 0.0;
		double needed = 0.0;
		for (ADSupply s : equip) {
			current += s.current.get(a);
			needed += s.targetAmount(a);
		}

		if (needed == 0) {
			return 1;
		}
		return current / needed;
	}

	public void transfer(WDIV div, WArmy old, WArmy current) {
		if (old == null || current == null)
			return;
		for (ADSupply s : all)
			s.transfer(div, old, current);

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
					super.set(t, CLAMP.i(s, 0, max(t)));
					artilleryTot.inc(t, get(t));
					for (ADSupply ss : supplies) {
						ss.setChanged(t);
					}
				}

				@Override
				public int max(WArmy t) {
					return get(t)+artilleryMax-artilleryTot.get(t);
				}

			};

			for (int i = 0; i < art.constructor().resources(); i++) {
				RESOURCE res = art.constructor().resource(i);
				ADSupply.ADSupplyArt sup = new ADSupplyArt(sups.size(), init, this, res, i);
				supplies.add(sup);
				sups.add(sup);
			}

		}

		public int current(WArmy a) {
			double d = 1.0;
			for (ADSupply ss : supplies) {
				d = Math.min(d, ss.amountValue(a));
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