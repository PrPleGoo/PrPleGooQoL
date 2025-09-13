package world.region.pop;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.boosting.BoosterImp;
import game.faction.FACTIONS;
import game.time.TIME;
import game.values.GVALUES;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.text.D;
import init.type.CLIMATES;
import init.type.HCLASSES;
import init.type.TERRAINS;
import prplegoo.regions.api.npc.KingLevels;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sprite.text.Str;
import util.data.DOUBLE_O;
import util.dic.Dic;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RBooster;
import world.region.RD;
import world.region.RD.RDInit;
import world.region.RD.RDUpdatable;
import world.region.RData.RDataE;

public class RDRace implements INDEXED{

	private static CharSequence ¤¤PopulationTarget = "Pop. Target";
	private static CharSequence ¤¤RulingSpecies = "¤Ruling Species";


	private static CharSequence ¤¤Biome = "¤Species Biome";

	private static CharSequence ¤¤Armies = "¤Army presence";
	private static CharSequence ¤¤Representation = "¤Representation";


	static {
		D.ts(RDRace.class);
	}

	public final Race race;

	public final RDRacePopulation pop;
	public final RDRaceLoyalty loyalty;

	private int index;

	public final RDNames names;

	RDRace(Race race, RDInit init, int index){
		this.race = race;

		this.index = index;
		double maxPop = 0.01;
		double growth = 0.01;

		names = new RDNames(race, init);



		maxPop = race.population().max;
		growth = race.population().growth;



		pop = new RDRacePopulation(init, this, maxPop, growth);
		loyalty = new RDRaceLoyalty(init, this);


	}

	public double loyaltyTarget(Region reg) {
		return loyalty.get(reg) > 1 ? 1 : 0;
	}

	@Override
	public int index() {
		return index;
	}


	public static final class RDRaceLoyalty extends RDataE implements RDUpdatable {

		public final Boostable target;

		private static final double DTime = 8.0/(TIME.secondsPerDay);
		RDRaceLoyalty(RDInit init, RDRace race) {
			super("RACE_LOYALTY" + race.race.key, init.count.new DataByte("RACE_LOYALTY" + race.race.key),
					init, RDRaces.¤¤Loyalty + ": " + race.race.info.names);
			target = BOOSTING.push("LOYALTY_" + race.race.key, 0, name, name, race.race.appearance().iconBig,  BoostableCat.ALL().WORLD_CIVICS);
			init.upers.add(this);
			new RBooster(new BSourceInfo(STATS.ENV().OTHERS.info().name, UI.icons().s.citizen), 0.75, 1.0, true) {
				@Override
				public double get(Region t) {
					double tot = RD.RACES().popTarget.getD(t);
					if (tot == 0)
						return 0;
					double rr = 0;
					for (RDRace o : RD.RACES().all) {
						rr += o.pop.target(t)*(race.race.pref().race(o.race));
					}
					return CLAMP.d(rr/tot, 0, 1);
				}
			}.add(target);

			new RBooster(new BSourceInfo(Dic.¤¤Population, UI.icons().s.human), 0, -10.0, false) {

				@Override
				public double get(Region t) {
					double d = (double)RD.RACES().popTarget.getD(t)/(1.0+RD.RACES().maxPop());
					d = (int)(d*100)/100.0;
					return d;
				}
			}.add(target);

			new RBooster(new BSourceInfo(¤¤Armies, UI.icons().s.sword), 0, 20, false) {
				@Override
				public double get(Region t) {
					if (KingLevels.isActive() && FACTIONS.player() != t.faction()) {
						return 0;
					}

					double power = 0;
					for (WArmy a : WORLD.ENTITIES().armies.fill(t))
						if (a.faction() == t.faction())
							power +=  AD.power().get(a);
					return (power)/(RD.RACES().pop.get(t)+1);
				}
			}.add(target);

			new RBooster(new BSourceInfo(¤¤Representation + " (" + Dic.¤¤Capitol, UI.icons().s.human), 0.75, 1.25, true) {

				@Override
				protected double get(Region reg) {
					int cit = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race.race);
					int slaves = STATS.POP().POP.data(HCLASSES.SLAVE()).get(race.race);
					int tot = STATS.POP().POP.data().get(null) + 1;
					if (cit == 0) {
						if (slaves > 0)
							return 0.5-CLAMP.d(250.0*slaves/tot, 0, 0.5);
						return 0.5;
					}else {
						return CLAMP.d(0.5 + 0.5*RACES.playable().size()*cit/tot, 0.5, 1);
					}
				}
			}.add(target);

			new RBooster(new BSourceInfo(¤¤Representation + ": " + HCLASSES.NOBLE().names, UI.icons().s.noble), 0.75, 1.25, true) {

				@Override
				protected double get(Region reg) {
					int cit = STATS.POP().POP.data(HCLASSES.NOBLE()).get(race.race);
					int tot = STATS.POP().POP.data(HCLASSES.NOBLE()).get(null);
					if (cit == 0) {
						if (tot > RACES.playable().size())
							return CLAMP.d(0.5-(tot-RACES.playable().size())/4.0, 0, 0.5);
						return 0.5;
					}else {
						return CLAMP.d(0.5 + 0.5*cit/tot, 0.5, 1.0);
					}
				}
			}.add(target);

			BOOSTING.connecter(new ACTION() {

				@Override
				public void exe() {


					double to = STATS.BATTLE().WAR.standing.definition(race.race).mul;
					if (STATS.BATTLE().WAR.standing.definition(race.race).inverted)
						to = -to;

					new RBooster(new BSourceInfo(STATS.BATTLE().WAR.info().name, UI.icons().s.sword), 0, to, false) {

						@Override
						protected double get(Region reg) {
							return CLAMP.d(STATS.BATTLE().WAR.data(HCLASSES.CITIZEN()).getD(race.race, 0), 0, 1);
						}
					}.add(target);

				}
			});

		}

		@Override
		public void update(Region reg, double time) {

			double d = increase(reg)*DTime*time;
			moveTo(reg, Math.abs(d), d < 0 ? 0 : 255);
		}

		public double increase(Region reg) {
			return (int)(target.get(reg)*10)/10.0;
		}

		@Override
		public void init(Region reg) {
			double d = target.get(reg);
			set(reg, d < 0 ? 0 : 255);
		}

	}

	public static final class RDRacePopulation extends RDataEFix implements RDUpdatable {

		public final double maxPopulation;
		public final double growthBase;
		public final Boostable dtarget;
		//		private Boostable<Region> targetBase;
		public final Boostable growth;
		private static final double DTime = 1.0/TIME.secondsPerDay;
		private final BoosterImp biome;

		RDRacePopulation(RDInit init, RDRace race, double max, double growthBase) {
			super("RACEPOP" + race.race.key, init.count.new DataShortE("RACEPOP" + race.race.key), init, Dic.¤¤Population + ": " + race.race.info.names);
			init.upers.add(this);
			maxPopulation = max;
			this.growthBase = growthBase;
			dtarget = BOOSTING.push("POPULATION_TARGET_" + race.race.key, 1, ¤¤PopulationTarget + ": " + race.race.info.names, race.race.info.names, race.race.appearance().iconBig, BoostableCat.ALL().WORLD_CIVICS);
			growth = BOOSTING.push("POPULATION_GROWTH_" + race.race.key, 1, Dic.¤¤Growth + ": " + race.race.info.names, race.race.info.names, race.race.appearance().iconBig, BoostableCat.ALL().WORLD_CIVICS);
			biome = new RBooster(new BSourceInfo(¤¤Biome, UI.icons().s.temperature), 0.1, 2, true) {
				@Override
				public double get(Region reg) {

					double c = 0;
					for (int i = 0; i < CLIMATES.ALL().size(); i++)
						c += reg.info.climate(CLIMATES.ALL().get(i))*race.race.population().climate(CLIMATES.ALL().get(i));

					double t = 0;
					for (int i = 0; i < TERRAINS.ALL().size(); i++)
						t += reg.info.terrain(TERRAINS.ALL().get(i))*race.race.population().terrain(TERRAINS.ALL().get(i));
					return c*t;
				}
			};
			biome.add(dtarget);



			new RBooster(new BSourceInfo(¤¤RulingSpecies, UI.icons().s.crown), 1, 1.2, true) {
				@Override
				public double get(Region t) {
					if (t.faction() != null && t.faction().race() == race.race)
						return 1;
					return 0;
				}
			}.add(dtarget);

			new RBooster(new BSourceInfo(Dic.¤¤Base, UI.icons().s.cancel), 0, 1, true) {
				@Override
				public double get(Region t) {
					return growthBase;
				}
			}.add(growth);

//			new RBooster(new BSourceInfo(RDRaces.¤¤Loyalty + ": " + race.race.info.names, UI.icons().s.happy), 0, 10, true) {
//				@Override
//				public double get(Region t) {
//					if (t.faction() == FACTIONS.player())
//						return CLAMP.d(race.loyalty.getD(t), 0, 10)/10.0;
//					return 1;
//				}
//			}.add(growth);



			GVALUES.REGION.pushI("POPULATION_RACE_" + race.race.key, Dic.¤¤Population + ": " + race.race.info.names, race.race.appearance().iconBig, this);
		}

		@Override
		public void update(Region reg, double time) {
			int t = target(reg);
			int pop = get(reg);

			if(t > pop) {
				double pp = (pop+10)* Math.max(0, growth(reg))*time*DTime;
				int inc = (int) pp;
				if (pp - inc > RND.rFloat())
					inc++;
				pop += inc;
				if (pop > t)
					pop = t;
				set(reg, pop);
			}else {
				double pp = (pop+10)*time*DTime;
				int inc = (int) pp;
				if (pp - inc > RND.rFloat())
					inc++;
				pop -= inc;
				if (pop < t)
					pop = t;
				set(reg, pop);
			}
		}


		@Override
		public void init(Region reg) {
			RDRacePopulation.clearCaache();
			set(reg, target(reg));
		}

		public int target(Region reg) {
			double d = dtarget(reg);
			d *= RD.RACES().capacity(reg);
			d *= maxPopulation;
			d = CLAMP.d(d, 0, 50000);
			return (int)Math.round(d);
		}

		public double dtarget(Region reg) {
			double d = dtarget.get(reg);
			double tot = totdTarget.getD(reg);
			if (tot > 0)
				d /= tot;
			return Math.round(d*100)/100.0;
		}

		public double growth(Region reg) {
			double n = get(reg);
			int t = target(reg);
			if (t == n)
				return 0;
			if (t < n) {
				double d = (t-n)/n;
				return d;
			}else {
				return growth.get(reg);
			}
		}

		public double base(Region reg) {
			return biome.get(reg);
		}

		private static int upI = -1;
		static void clearCaache() {
			upI = -1;
		}

		@Override
		public void set(Region t, int i) {
			super.set(t, i);
		}

		private static final DOUBLE_O<Region> totdTarget = new DOUBLE_O<Region>() {


			private Region upR = null;
			private double cache;

			@Override
			public double getD(Region t) {
				if (upI != GAME.updateI() || upR != t) {
					upI = GAME.updateI();
					upR = t;
					cache = 0;
					for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
						cache += RD.RACES().all.get(ri).pop.dtarget.get(t);
					}
				}
				return cache;
			}
		};


		private static INT_OE<Region> count(RDInit init, Race race){
			return init.count.new DataShortE("RACEPOP" + race.key) {

				@Override
				public void set(Region t, int s) {
					RD.RACES().pop.set(t, RD.RACES().population.get(t)-get(t));
					super.set(t, s);
					RD.RACES().pop.set(t, RD.RACES().population.get(t)+get(t));
				}

			};
		}

	}



	public static class RDNames {


		public final RDNameList intros;
		public final RDNameList fNames;
		public final RDNameList rIntro;
		public final RDNameList rNames;

		RDNames(Race r, RDInit init){
			intros = new RDNameList(r.info.winfo.intros);
			fNames = new RDNameList(r.info.winfo.fNames);
			rIntro = new RDNameList(r.info.winfo.rIntro);
			rNames = new RDNameList(r.info.winfo.rNames);

			init.savable.add(new SAVABLE() {

				@Override
				public void save(FilePutter file) {
					file.i(intros.i);
					file.i(fNames.i);
					file.i(rIntro.i);
					file.i(rNames.i);

				}

				@Override
				public void load(FileGetter file) throws IOException {
					intros.i = file.i();
					fNames.i = file.i();
					rIntro.i = file.i();
					rNames.i = file.i();
				}

				@Override
				public void clear() {
					// TODO Auto-generated method stub

				}
			});
		}



	}

	public static class RDNameList{

		private int i = 0;
		private final ArrayList<String> all;

		private RDNameList(String[] nn) {
			all = new ArrayList<String>(nn);
			i = RND.rInt(all.size());
		}

		public String next() {
			i%= all.size();
			String s = all.get(i);
			if (FACTIONS.player() != null && Str.isSame(s, FACTIONS.player().name)) {
				i++;
				i%= all.size();
				s = all.get(i);
			}
			i++;
			return s;
		}

		public String get(int index) {
			return all.get(index);
		}

	}



}