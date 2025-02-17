package world.region.pop;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.npc.FactionNPC;
import game.values.GVALUES;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.text.D;
import settlement.entity.ENTETIES;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DoubleOCached;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.dic.Dic;
import util.info.INFO;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD.RDInit;
import world.region.RData;
import world.region.RData.RDataE;

public class RDRaces {



	public static CharSequence ¤¤Loyalty = "¤Loyalty";
	public static CharSequence ¤¤LoyaltyD = "¤Current Loyalty. Loyalty determines the chance of rebellion. Loyalty changes slowly based on the target. Increase loyalty by allocating admin points into loyalty boosting areas. Loyalty will also increase the longer a region has belonged to you. Loyalty is species specific.";
	private static CharSequence ¤¤RegionCapacity = "¤Region Capacity";
	private static CharSequence ¤¤RegionCapacityD = "¤Region Population capacity.";
	{
		D.ts(RDRaces.class);
	}

	private final RDRace[] map = new RDRace[RACES.all().size()];
	public final LIST<RDRace> all;
	public final Boostable capacity;
	public final RDEdicts edicts;
	final RDataE pop;
	public final RData population;
	public final Visuals visuals;

	private final double maxArea = 10*10;
	private final double aveFertility = 0.5;
	private final double maxFerArea = maxArea*aveFertility;

	private double maxPop = 50000;
	private double maxPopI = 1.0/maxPop;

	private void setMax(int max) {
		maxPop = max;
		maxPopI = 1.0/maxPop;
	}

	public final DoubleOCached<Region> popTarget = new DoubleOCached<Region>() {

		@Override
		public double getValue(Region t) {
			double cache = 0;
			for (int ri = 0; ri < all.size(); ri++) {
				cache += all.get(ri).pop.target(t);
			}
			return cache;
		}
	};

	public final DOUBLE_O<Region> loyaltyAll = new DOUBLE_O<Region>(){

		final INFO info = new INFO(¤¤Loyalty, ¤¤LoyaltyD);

		@Override
		public double getD(Region t) {
			double d = 0;
			for (RDRace r : all) {
				d += r.pop.get(t)*r.loyalty.getD(t);
			}
			if (population.get(t) > 0)
				d /= population.get(t);
			return d;
		}

		@Override
		public INFO info() {
			return info;
		};


	};

	public RDRaces(RDInit init){


		capacity = BOOSTING.push("POPULATION_CAPACITY", 1, ¤¤RegionCapacity, ¤¤RegionCapacityD, UI.icons().s.human, BoostableCat.ALL().WORLD, 1.0);
		new RBooster(new BSourceInfo(Dic.¤¤Area, UI.icons().s.expand), 0, 100, true) {

			@Override
			public double get(Region t) {
				return (double)t.info.area()/maxArea;
			}


		}.add(capacity);
		new RBooster(new BSourceInfo(Dic.¤¤Fertility, UI.icons().s.sprout), 0.2, 1, true) {

			@Override
			public double get(Region t) {
				return t.info.fertility();
			}



		}.add(capacity);

		pop = new RDataE("POPULATION", init.count.new DataInt("POPULATION"), init, Dic.¤¤Population);
		population = pop;
		GVALUES.REGION.pushI("POPULATION", Dic.¤¤Population, UI.icons().s.human, pop);
		GVALUES.REGION.pushI("POPULATION_KINGDOM", Dic.¤¤Population + ": " + Dic.¤¤Realm, UI.icons().s.human, new INT_O<Region>() {

			@Override
			public int get(Region t) {
				if (t.faction() == null)
					return 0;
				return pop.faction().get(t.faction());
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

		ArrayList<RDRace> all = new ArrayList<RDRace>(RACES.playable().size());

		for (Race r : RACES.playable()) {
			RDRace rr = new RDRace(r, init, all.size());
			map[r.index()] = rr;
			all.add(map[r.index()]);


		}

		this.all = all;
		visuals = new Visuals(init);

		edicts = new RDEdicts(all, init);



	}

	public void init() {
		double m = capacity.max(Region.class)*1.5;
		setMax((int) m);
	}

//	public void initPopulation(Region reg) {
//		RDRacePopulation.clearCaache();
//		
//		for (RDRace r : all) {
//			r.pop.set(reg, (int) Math.round(r.pop.target(reg)));
//		}
//
//	}

	public RDRace get(Race race) {
		return map[race.index];
	}

	public double maxPop() {
		return maxPop;
	}

	public double popSizeD(Region reg) {
		if (reg.capitol()) {
			return population.get(reg)/(double)ENTETIES.MAX;
		}
		return CLAMP.d(population.get(reg)*maxPopI*(maxArea/reg.info.area()), 0, 1);
	}

	public double popSize(Region reg) {
		if (reg.capitol()) {
			return population.get(reg)/(double)ENTETIES.MAX;
		}
		return CLAMP.d(population.get(reg)*maxPopI, 0, 1);
	}

	public double popSizeTarget(Region reg) {
		if (reg.capitol()) {
			return popTarget.getD(reg)/(double)ENTETIES.MAX;
		}
		return CLAMP.d(popTarget.getD(reg)*maxPopI, 0, 1);
	}

	double capacity(Region reg) {

		if (reg.faction() instanceof FactionNPC) {

			double fa = maxPop*reg.info.area()*reg.info.fertility()/maxFerArea;
			double min = fa*0.1;
			double max = fa;

			FactionNPC f = (FactionNPC) reg.faction();


			double empireSize = f.realm().ferArea()/(10*maxFerArea);
			empireSize = CLAMP.d(empireSize, 0, 1);

			double competence = (0.25 + 0.75*f.court().king().size());

			if (reg.capitol()) {
				return min +(ENTETIES.MAX-min)*competence*empireSize;
			}

			return min +(max-min)*competence*Math.pow(empireSize, 0.5);
		}else if (reg.faction() == null) {
			double fa = maxPop*reg.info.area()*reg.info.fertility()/maxFerArea;
			return fa*0.1;
		}
		return capacity.get(reg);


	}


	public final class Visuals {

		private final INT_OE<Region> cRace;
		private final INT_OE<Region> cacheI;
		private final ArrayList<INT_OE<Region>> vVill = new ArrayList<INT_OE<Region>>(16);

		private Visuals(RDInit init) {
			if (all.size() > 255)
				throw new RuntimeException("too many races");
			cRace = init.count.new DataByte("VISUALS_RACE");
			cacheI = init.count.new DataNibble("VISUALS_RACEI");
			while(vVill.hasRoom())
				vVill.add(init.count.new DataByte("VISUALS_RACE?" + vVill.size()));
		}

		public Race cRace(Region reg) {
			cache(reg);
			return all.get(cRace.get(reg)).race;
		}

		public Race vRace(Region reg, int ran) {
			cache(reg);
			ran &= 0x0F;
			return all.get(vVill.get(ran).get(reg)).race;
		}

		private void cache(Region reg) {
			int ri = (0x0F-((VIEW.RI()>>6)&0x0F));
			if (cacheI.get(reg) == ri)
				return;
			cacheI.set(reg, ri);
			RDRace biggest = null;
			int bb = -1;
			int vi = 0;
			for (int rri = 0; rri < all.size(); rri++) {
				RDRace r = all.get(rri);
				if (r.pop.get(reg) > bb) {
					biggest = r;
					bb = r.pop.get(reg);
				}
				if (population.get(reg) > 0) {
					int vam = 16*r.pop.get(reg)/population.get(reg);
					for (int i = 0; i < vam && vi < 16; i++) {
						vVill.get(vi++).set(reg, r.index());
					}
				}
			}

			cRace.set(reg, biggest.index());

			for (; vi < 16; vi++) {
				vVill.get(vi).set(reg, biggest.index());
			}

		}

	}

}