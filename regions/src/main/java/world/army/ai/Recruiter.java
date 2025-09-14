package world.army.ai;

import game.boosting.BOOSTABLES;
import game.faction.npc.FactionNPC;
import init.config.Config;
import init.race.RACES;
import init.race.Race;
import prplegoo.regions.api.npc.KingLevels;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import world.WORLD;
import world.army.*;
import world.entity.army.WArmy;
import world.map.regions.Region;

final class Recruiter {

	private final Tree<WArmy> tree = new Tree<WArmy>(100) {

		@Override
		protected boolean isGreaterThan(WArmy current, WArmy cmp) {
			if (AD.menTarget(null).get(current) > AD.menTarget(null).get(cmp))
				return true;
			return current.armyIndex() > cmp.armyIndex();
		}
	
	};

	public void recruit(FactionNPC f) {
		
		if (f.realm().all().size() == 0)
			return;
		
		int menTarget = (int) (AD.conscripts().available(null).get(f));
		int men = AD.menTarget(null).faction(f);
		int recruits = menTarget-men;
		if (recruits < 10 && !KingLevels.isActive())
			return;


		int armies = CLAMP.i(1 + menTarget/5000, 1, 3);

		while(f.armies().all().size() < armies && f.armies().canCreate()) {
			Region r = f.realm().all().rnd();
			COORDINATE c = WORLD.PATH().rnd(r);
			WORLD.ENTITIES().armies.create(c.x(), c.y(), f);
		}
		
		
		tree.clear();
		for (int ai = 0; ai < f.armies().all().size(); ai++) {
			WArmy a = f.armies().all().get(ai);
			tree.add(a);
		}
		
		while(tree.hasMore()) {
			WArmy a = tree.pollGreatest();

			if (KingLevels.isActive() && a.divs().size() < 2) {
				AD.supplies().arts().rnd().target.set(a, 0);
			}

			if (KingLevels.isActive() && a.divs().size() > 1) {
				int randomDivisionIndex = RND.rInt(a.divs().size());
				ADDiv randomDivision = a.divs().get(randomDivisionIndex);

				if (randomDivision.men() != 0) {
					if (AD.supplies().health(a) < 1) {
						randomDivision.disband();
						break;
					}
				}

				for (int i = 0; i < a.divs().size(); i++) {
					ADDiv div = a.divs().get(i);
					if (RND.oneIn(4) && div.men() != 0 && isMissingEquips(a, div)) {
						div.disband();
					}
				}

				if (AD.supplies().equip(a) < 0.75
					|| AD.supplies().supplyEquip(a) < 0.75) {
					break;
				}
			}



			int target = menTarget;
			if (tree.hasMore()) {
				target *= 0.75;
			}
			
			target = CLAMP.i(target, 0, Config.BATTLE.MEN_PER_ARMY);
			if (target > AD.menTarget(null).get(a)) {
				recruit(f, a, target);
			}
			
			menTarget -= AD.menTarget(null).get(a);
			
		}

		
	}

	private boolean isMissingEquips(WArmy a, ADDiv div) {
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			if (div.equipTarget(e) != 0
					&& AD.supplies().get(e).amountValue(a) < 0.25) {
				return true;
			}
		}

		return false;
	}

	private void recruit(FactionNPC f, WArmy a, int target) {
		
		main:
		while(AD.menTarget(null).get(a) < target && a.divs().canAdd()) {
			int ri = RND.rInt(RACES.all().size());
			
			for (int i = 0; i < RACES.all().size(); i++) {
				Race r = RACES.all().get((ri+i)%RACES.all().size());
				
				int am = AD.conscripts().available(r).get(f);
				
				am = CLAMP.i(am, 0, Config.BATTLE.MEN_PER_DIVISION);

				if (KingLevels.isActive()) {
					if (am < 15) {
						continue;
					}

					double maxDivisionSize = AD.conscripts().available(null).get(f) / 7.0;
					am = CLAMP.i(am, 15, (int) maxDivisionSize);
				}
				
				int min = (int) (Config.BATTLE.MEN_PER_DIVISION*a.divs().size()/10.0);
				min = Math.min(min, Config.BATTLE.MEN_PER_DIVISION);
				if (am < min)
					continue;
				
				if (am > 0) {
					if (r.playable)
						am = CLAMP.i(am, 5, Config.BATTLE.MEN_PER_DIVISION);
					
					double trai = 0.1 + 0.9*0.25*BOOSTABLES.NOBLE().AGRESSION.get(f.court().king().roy().induvidual);
					double equip = 0.1 + 0.9*0.5*BOOSTABLES.NOBLE().COMPETANCE.get(f.court().king().roy().induvidual);
					WDivRegional d = AD.regional().create(r, (double)am/Config.BATTLE.MEN_PER_DIVISION, a);
					d.randomize(trai, equip);
					//d.menSet(d.menTarget());

					if (KingLevels.isActive()) {
						break main;
					}

					continue main;
				}
			}
			break;
		}
		
		int arts = AD.menTarget(null).get(a);
		arts /= 200 + RND.rInt(100);
		arts += RND.rInt(2);
		arts = CLAMP.i(arts, 0, ADSupplies.artilleryMax);
		
		while(arts > 0) {
			arts--;
			AD.supplies().arts().rnd().target.inc(a, 1);
		}
		
	}
	

}
