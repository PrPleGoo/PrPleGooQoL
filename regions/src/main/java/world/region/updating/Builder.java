package world.region.updating;


import java.util.Random;

import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.race.Race;
import init.religion.Religion;
import init.resources.RESOURCE;
import prplegoo.regions.api.npc.KingLevels;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tree;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDResource;
import world.region.RDReligions.RDReligion;
import world.region.building.RDBuilding;
import world.region.pop.RDEdicts.RDRaceEdict;
import world.region.pop.RDRace;

final class Builder {

	private final Resources res = new Resources();
	private final BRace race = new BRace();
	private final BReligion religion = new BReligion();
	private final BMil military = new BMil();
	private final BOther civic;

	private final RebelBuilder rebBuilder = new RebelBuilder();

	Builder(){
		LinkedList<RDBuilding> all = new LinkedList<>();
		for (RDBuilding bu : RD.BUILDINGS().all) {
			all.add(bu);
		}

		for (RBuilding<?> b : res.all)
			all.remove(b.bu);

		for (RBuilding<?> b : race.all)
			all.remove(b.bu);

		for (RBuilding<?> b : religion.all)
			all.remove(b.bu);

		for (RBuilding<?> b : military.all)
			all.remove(b.bu);

		civic = new BOther(all);
	}

	public void build(Region reg) {
		RealmBuilder builder = rebBuilder;
		if (reg.faction() instanceof FactionNPC) {
			if (KingLevels.getInstance().getBuilder().build((FactionNPC) reg.faction())) {
				return;
			}
			builder = ((FactionNPC)reg.faction()).court().king().builder;
		}
		build(reg, builder);
	}

	public void build(Region reg, RealmBuilder builder) {
		if (builder == null)
			builder = rebBuilder;

		if (RD.OWNER().prevOwner(reg) == FACTIONS.player()) {
			for (RDBuilding bu : RD.BUILDINGS().all) {
				if (bu.level.get(reg) > 0) {
					bu.level.set(reg, 0);
					return;
				}
			}
		}

		for (RDBuilding bu : RD.BUILDINGS().all) {
			bu.level.set(reg, 0);
		}

		for (RDRace rr : RD.RACES().all) {
			for (RDRaceEdict e : RD.RACES().edicts.all)
				e.toggled(rr).set(reg, 0);
		}

		res.build(reg, builder);
		race.build(reg, builder);
		religion.build(reg, builder);
		military.build(reg, builder);
		civic.build(reg, builder);
	}


	private static class BOther {

		private final Sort<Integer> tree;
		private double[] prios = new double[256];

		BOther(LIST<RDBuilding> all){

			Random ran = new Random(12345678910l);
			for (int i = 0; i < prios.length; i++) {
				prios[i] = ran.nextDouble();
			}

			LinkedList<RBuilding<Integer>> aa = new LinkedList<>();
			for (RDBuilding b : all) {
				if (!b.AIBuild)
					continue;
				aa.add(new RBuilding<Integer>(b) {

					@Override
					public double value(RealmBuilder current, Region rcurrent) {
						int i = bu.index() + RD.RAN().get(rcurrent, 0, 16);
						i &= 255;
						return prios[i];
					}

					@Override
					double value(Integer t, RealmBuilder builder, Region reg) {
						// TODO Auto-generated method stub
						return 0;
					}

				});
			}

			tree = new Sort<Integer>(aa);

		}

		void build(Region reg, RealmBuilder builder) {

			tree.build(reg, builder, points(builder, reg, 1.0));
		}

	}

	private static class BMil {

		private final Sort<Integer> tree;
		private double[] prios = new double[256];
		private LinkedList<RBuilding<Integer>> all = new LinkedList<>();

		BMil(){

			Random ran = new Random(12345678910l);
			for (int i = 0; i < prios.length; i++) {
				prios[i] = ran.nextDouble();
			}

			//LinkedList<RBuilding<Integer>> aa = new LinkedList<>();
			for (RDBuilding b : RD.BUILDINGS().all) {
				double v = Math.max(b.boosters().max(RD.MILITARY().bgarrison), b.boosters().max(RD.MILITARY().bFortification));
				if (!b.AIBuild || v <= 0)
					continue;

				all.add(new RBuilding<Integer>(b) {

					@Override
					public double value(RealmBuilder current, Region rcurrent) {
						int i = bu.index() + RD.RAN().get(rcurrent, 0, 16);
						i &= 255;
						return prios[i]*current.military(rcurrent);
					}

					@Override
					double value(Integer t, RealmBuilder builder, Region reg) {
						// TODO Auto-generated method stub
						return 0;
					}

				});
			}

			tree = new Sort<Integer>(all);

		}

		void build(Region reg, RealmBuilder builder) {

			tree.build(reg, builder, points(builder, reg, 1.0));
		}

	}

	private static int points(RealmBuilder builder, Region reg, double am) {
		int p = (int) (builder.size()*RD.RACES().population.get(reg)/(100));
		return p;
	}


	private static class BReligion {

		private LinkedList<RBuilding<Religion>> all = new LinkedList<>();
		private final Sort<Religion> tree;

		BReligion(){

			final KeyMap<Religion> boosts = new KeyMap<>();
			for (RDReligion rr : RD.RELIGION().all()) {
				boosts.put(rr.boost.key, rr.religion);
			}

			for (RDBuilding bu : RD.BUILDINGS().all) {
				RBuilding<Religion> br = new RBuilding<Religion>(bu) {
					@Override
					double value(Religion t, RealmBuilder builder, Region reg) {
						return builder.priority(t, reg);
					}
				};

				for (BoostSpec s : bu.boosters().all()) {

					if (boosts.containsKey(s.boostable.key)) {
						br.bos.add(new RSpec<Religion>(s, boosts.get(s.boostable.key)));
					}
				}
				if (bu.AIBuild && br.bos.size() > 0)
					all.add(br);
			}

			tree = new Sort<Religion>(all);
		}

		void build(Region reg, RealmBuilder builder) {
			tree.build(reg, builder, points(builder, reg, 1.0));
		}



	}

	private static class BRace {

		private LinkedList<RBuilding<RDRace>> all = new LinkedList<>();
		private final Sort<RDRace> tree;

		BRace(){

			KeyMap<RDRace> map = new KeyMap<>();

			for (RDRace r : RD.RACES().all) {
				map.put(r.pop.dtarget.key, r);
			}

			for (RDBuilding bu : RD.BUILDINGS().all) {
				RBuilding<RDRace> br = new RBuilding<RDRace>(bu) {
					@Override
					double value(RDRace t, RealmBuilder builder, Region reg) {
						return builder.policy(t.race, reg);
					}
				};
				for (BoostSpec s : bu.boosters().all()) {
					if (map.containsKey(s.boostable.key)) {
						br.bos.add(new RSpec<RDRace>(s, map.get(s.boostable.key)));
					}
				}
				if (bu.AIBuild && br.bos.size() > 0)
					all.add(br);
			}

			tree = new Sort<RDRace>(all);

		}

		void build(Region reg, RealmBuilder builder) {



			tree.build(reg, builder, points(builder, reg, 0.75));

			for (RDRace r : RD.RACES().all) {
				double v = builder.policy(r.race, reg);
				for (RDRaceEdict ee : RD.RACES().edicts.all) {
					ee.toggled(r).set(reg, 0);
				}

				if (v < 0) {
					v = -v;
					int i = (int) Math.round(v*RD.RACES().edicts.all.size())-1;
					i = CLAMP.i(i, 0, RD.RACES().edicts.all.size()-1);
					if (i >= 0) {
						RD.RACES().edicts.all.get(i).toggled(r).set(reg, 1);
					}else {

					}
				}
			}

		}

	}

	private static class Resources {

		private LinkedList<RBuilding<RDResource>> all = new LinkedList<>();
		private final Sort<RDResource> tree;

		Resources(){

			KeyMap<RDResource> map = new KeyMap<>();

			for (RDResource r : RD.OUTPUT().RES) {
				map.put(r.boost.key, r);
			}

			for (RDBuilding bu : RD.BUILDINGS().all) {
				RBuilding<RDResource> br = new RBuilding<RDResource>(bu) {
					@Override
					double value(RDResource t, RealmBuilder builder, Region reg) {
						if (false)
							;//no efficiency here
						return builder.priority(t.res, reg)*bu.efficiency.get(reg)*bu.efficiency.get(reg);
					}
				};
				for (BoostSpec s : bu.boosters().all()) {
					if (map.containsKey(s.boostable.key)) {
						br.bos.add(new RSpec<RDResource>(s, map.get(s.boostable.key)));
					}
				}
				if (bu.AIBuild && br.bos.size() > 0)
					all.add(br);
			}
			tree = new Sort<RDResource>(all);
		}



		void build(Region reg, RealmBuilder builder) {
			tree.build(reg, builder, points(builder, reg, 1.0));
		}

	}

	private abstract static class RBuilding<T> {

		public final RDBuilding bu;
		public final ArrayListGrower<RSpec<T>> bos = new ArrayListGrower<>();

		public RBuilding(RDBuilding bu) {
			this.bu = bu;
		}

		public double value(RealmBuilder current, Region rcurrent) {
			double v1 = 0;
			for (RSpec<T> b : bos) {
				v1 +=  b.bo.booster.getValue(1.0)*value(b.t, current, rcurrent);
			}
			return v1;
		}
		abstract double value(T t, RealmBuilder builder, Region reg);
	}

	private static class RSpec<T> {

		public final BoostSpec bo;
		public final T t;

		public RSpec(BoostSpec bo, T t) {
			this.bo = bo;
			this.t = t;
		}



	}

	private static class Sort<T> extends Tree<RBuilding<T>> {

		private final LIST<RBuilding<T>> all;

		public Sort(LIST<RBuilding<T>> all) {
			super(all.size());
			this.all = all;
		}

		private RealmBuilder current;
		private Region rcurrent;

		double init(Region reg, RealmBuilder builder) {
			current = builder;
			rcurrent = reg;
			clear();
			double vv = 0;
			for (RBuilding<T> b : all) {
				double v = b.value(builder, reg);
				if (v > 0) {
					vv += v;
					add(b);
				}

			}
			return vv;
		}

		@Override
		protected boolean isGreaterThan(RBuilding<T> curr, RBuilding<T> cmp) {
			return curr.value(current, rcurrent) > cmp.value(current, rcurrent);
		}

		void build(Region reg, RealmBuilder builder, int points) {

			double mid = init(reg, builder);

			while(hasMore() && points > 0) {
				RBuilding<?> b = pollGreatest();
				if (b.bu.level.get(reg) != 0)
					continue;
				double v = b.value(builder, reg);

				int l = (int) (Math.ceil((b.bu.levels.size()-1)*v/mid));
				l = CLAMP.i(l, 0, b.bu.levels.size()-1);
				l = CLAMP.i(l, 0, points);
				b.bu.level.set(reg, l);
				//b.bu.level.set(reg, l);
				points -= l;

			}

		}


	}

	private static class RebelBuilder implements RealmBuilder {

		private Region cacheReg = null;
		private double[] races = new double[RACES.all().size()];
		private double[] religions = new double[RACES.all().size()];
		private double mil;

		private void init(Region reg) {
			if (cacheReg == reg)
				return;
			cacheReg = reg;
			mil = RD.RAN().get(reg, 20, 8)/1024.0;

			double max = -Double.MAX_VALUE;
			RDRace mrace = null;
			int ri = RD.OWNER().ownerI.get(reg);
			for (RDRace rr : RD.RACES().all) {
				double d = rr.pop.base(reg)*(0.25+RD.RAN().get(reg, ri+rr.race.index()*3, 3)/7.0);
				double dr = RD.RAN().get(reg, ri+rr.race.index()*5, 4);
				dr /= 0b111;
				races[rr.race.index()] = -1.0 + dr;
				if (d > max) {
					mrace = rr;
					max = d;
				}
			}

			double rasism = STATS.ENV().OTHERS.standing().definitionD(mrace.race);

			for (RDRace rr : RD.RACES().all) {
				if (rr == mrace) {
					races[rr.race.index()] = 1 + 2*rasism;
					continue;
				}
				races[rr.race.index()] += mrace.race.pref().race(rr.race);
				races[rr.race.index()] *= rasism;
			}

			RDReligion tr = RD.RELIGION().all().get(0);
			max = 0;
			for (RDReligion rr : RD.RELIGION().all()) {
				religions[rr.religion.index()] = 0;
				if (rr.current.get(reg) > max) {
					max = rr.current.get(reg);
					tr = rr;
				}
			}
			religions[tr.religion.index()] = 1.0;


		}

		@Override
		public double policy(Race race, Region reg) {
			init(reg);
			return races[race.index()];
		}

		@Override
		public double priority(RESOURCE res, Region reg) {
			return 1.0;
		}

		@Override
		public double priority(Religion religion, Region reg) {
			init(reg);
			return religions[religion.index()];
		}

		@Override
		public double military(Region reg) {
			return mil;
		}

		@Override
		public double size() {
			return 0.25;
		}

	}




}
