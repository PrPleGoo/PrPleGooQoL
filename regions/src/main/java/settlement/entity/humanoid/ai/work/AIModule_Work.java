package settlement.entity.humanoid.ai.work;

import game.GAME;
import init.sprite.UI.UI;
import init.text.D;
import init.type.BUILDING_PREFS;
import init.type.HTYPES;
import init.type.WGROUP;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.room.food.farm.ROOM_FARM;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.food.hunter.ROOM_HUNTER;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.service.arena.grand.ROOM_ARENA;
import settlement.room.service.arena.pit.ROOM_FIGHTPIT;
import settlement.room.service.pleasure.ROOM_PLEASURE;
import settlement.room.spirit.grave.GraveData;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

import static settlement.main.SETT.ROOMS;

public final class AIModule_Work extends AIModule{


	private final PlanBlueprint[] map = new PlanBlueprint[ROOMS().all().size()];
	
	private final AIPLAN hangArround = new PlanHangArround("workHang");
	
	final PlanOddjobber oddjobber = new PlanOddjobber("workOdd");
	private final PlanFetchEquip equip = new PlanFetchEquip("workEquip");
	
	private static CharSequence ¤¤name = "Work";
	private static CharSequence ¤¤desc = "Spend time working";
	static {
		D.ts(AIModule_Work.class);
	}
	
	public AIModule_Work(){
		super(UI.icons().s.hammer, ¤¤name, ¤¤desc);
		Works w = new Works();
		
		for (ROOM_FARM  b : ROOMS().FARMS) {
			new WorkAbs(this, b, map, w) {
				@Override
				public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
					return b.shouldReportWorkFailure();
				}
			};
		}
		new WorkAbs(this, ROOMS().WOOD_CUTTER, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().MINES)
			new WorkAbs(this,b, map, w);
		for (RoomBlueprintIns<?> b : ROOMS().PASTURES)
			new WorkAbs(this,b, map, w);
		for (ROOM_FISHERY b : ROOMS().FISHERIES)
			new WorkFisherman(this,b, map, w);
		new WorkAbs(this, ROOMS().PRISON, map, w);
		new WorkAbs(this, ROOMS().ASYLUM, map, w);
		new WorkAbs(this, ROOMS().HOSPITAL, map, w);
		new WorkAbs(this, ROOMS().INN, map, w);
		new WorkDeliveryman(this, map, SETT.ROOMS().STOCKPILE, true);
		new WorkDeliveryman(this, map, ROOMS().HAULER, true);
		for (GraveData.GRAVE_DATA_HOLDER h : ROOMS().GRAVES)
			new WorkGraveDigger(this, map, h);
		new WorkAbs(this, ROOMS().JANITOR, map, w);
		new WorkDeliveryman(this, map, SETT.ROOMS().EXPORT, true);
		for (ROOM_HUNTER h : SETT.ROOMS().HUNTERS)
			new WorkHunter(h, this, map);
		for (ROOM_PLEASURE h : SETT.ROOMS().BROTHELS)
			new WorkHooker(h, this, map);
		new WorkCannibal(this, map);
		new WorkGuard(this, map);
		new WorkExecutioner(this, map);
		new WorkJudge(this, map);
		new WorkBuilder(this, map);
		new WorkSlaver(this, map);
		new WorkTransporter(this, map, w);
		new WorkTransporterSupply(this, map, w);
		new PrPleGooWorkTransporterLogistics(this, map, w);
		new WorkAbs(this, SETT.ROOMS().STATION, map, w);
		new WorkEmissary(this, map);
		new WorkAbs(this, SETT.ROOMS().CHAMBER, map, w);
		{
			for (RoomBlueprintIns<?> p : ROOMS().PHYSICIANS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().EATERIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().CANTEENS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().TAVERNS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LAVATORIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().BATHS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().WORKSHOPS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().REFINERS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LIBRARIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().LABORATORIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().ADMINS)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().NURSERIES)
				new WorkAbs(this, p, map, w);
			for (RoomBlueprintIns<?> p : ROOMS().SCHOOLS)
				new WorkAbs(this, p, map, w);
			for (ROOM_TEMPLE p : ROOMS().TEMPLES.ALL)
				new WorkTemple(this, p, map);
		}

		for (RoomBlueprintIns<?> p : ROOMS().SPEAKERS)
			WorkOrator.getSpeaker(this, p, map);
		
		for (RoomBlueprintIns<?> p : ROOMS().STAGES)
			WorkOrator.getDancer(this, p, map);
	
		for (ROOM_FIGHTPIT b : SETT.ROOMS().FIGHTPITS) {
			new WorkGladiator(b.work, b, this, map);
		}
		
		for (ROOM_ARENA b : SETT.ROOMS().GARENAS) {
			new WorkGladiator(b.work, b, this, map);
		}
		
		for (RoomBlueprint b : SETT.ROOMS().all()) {
			if ( b instanceof RoomBlueprintIns<?>) {
				RoomBlueprintIns<?> p = (RoomBlueprintIns<?>) b;
				if (p.employmentExtra() != null && map[p.index()] == null)
					new WorkAbs(this, p, map, w);
			}
		}
		

		
//		new RoomWorker(ROOMS().CONSTRUCTION.jobTitle, ROOMS().CONSTRUCTION);

		
	}
	

	
	

	
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		AIModules.data().byte2.set(d, 0);
		STATS.WORK().slackStart(a);
		if (!validateEmployment(a, d)) {
			if (!PlanOddjobber.hasOddjob(a, true))
				return null;
			if (SETT.ARMIES().enemy().men() > 0)
				return null;
			AiPlanActivation p = oddjobber.activateOddjobber(a, d);
			return p;
		}
		swapper.swap(a);
		PlanBlueprint b = map[work(a).blueprint().index()];
		if (b == null) {
			throw new RuntimeException(""+work(a).blueprintI().info.name);
		}
		
		{
			AiPlanActivation p = equip.activate(a, d);
			if (p != null)
				return p;
		}
		
		
		AiPlanActivation p = b.activate(a, d);
		if (p == null) {
			
			
			if (b.shouldReportWorkFailure(a, d))
				AIModules.data().byte2.set(d, 1);
			if (PlanOddjobber.hasOddjob(a, false) && SETT.ARMIES().enemy().men() == 0) {
				p = oddjobber.activateHelpOut(a, d);
				if (p != null)
					return p;
				
			}
			if (p == null) {
				return hangArround.activate(a, d);
			}
			
		}else {
			AIModules.data().byte2.set(d, 2);
		}
		
		return p;
	}
	
	public void swapInstance(Humanoid a) {
		swapper.swap(a);
	}
	
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
	}
	
	@Override
	protected void finish(Humanoid a, AIManager d) {
		int i = AIModules.data().byte2.get(d);
		if (i == 1) {
			STATS.WORK().slackEnd(a, true);
		}else if (i == 2) {
			STATS.WORK().slackEnd(a, false);
		}
		super.finish(a, d);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (work(a) == null && !ROOMS().employment.hasWork(a)) {
			if (SETT.ARMIES().enemy().men() > 0)
				return 0;
			if (!PlanOddjobber.hasOddjob(a, true))
				return 0;
			
		}
		if (GAME.events().riot.onStrike(a))
			return 0;
		
		if (STATS.WORK().getWorkPriority(a) > 0) {
			return 5;
		}
		
		return 0;
	}
	
	public int getPriority(Humanoid a) {
		return getPriority(a, (AIManager) a.ai());
	}
	
	private static RoomInstance work(Humanoid a) {
		return STATS.WORK().EMPLOYED.get(a.indu());
	}

	private final Swapper swapper = new Swapper();
	private boolean validateEmployment(Humanoid a, AIManager d) {
		ROOMS().employment.setWork(a);
	
		
		
		if(work(a) != null && work(a).acceptsWork() && map[work(a).blueprint().index()] != null) {
			
			return true;
		}
		return false;
	}
	
	public boolean isLawEnforcement(Humanoid a, AIManager d) {
		return a.indu().hType() == HTYPES.RECRUIT() || (work(a) != null && (map[work(a).blueprint().index()] instanceof WorkGuard));
	}

	public static double getTransportAmount(Humanoid a) {
		AIManager d = (AIManager) a.ai();
		if (d.plan() instanceof PlanBlueprint) {
			PlanBlueprint t = (PlanBlueprint) d.plan();
			return t.transportAmount(a, d);
		}
		return -1;
	}
	
	private static class Swapper {
		
		private W[] misplaced = new W[SETT.ROOMS().employment.ALLS().size()];
		
		Swapper(){
			for (RoomEmploymentSimple s : SETT.ROOMS().employment.ALLS()) {
				misplaced[s.eindex()] = new W(s);
			}
		}
		
		private void swap(Humanoid h) {
			final RoomInstance w = work(h);
			
			WGROUP group = group(h);
			double p = stayPriority(group, w);
			if (p >= 1)
				return;
			
			W wer = misplaced[w.blueprintI().employment().eindex()];
			
			
			
			{
				
				
				RoomInstance w2 = updateBestRoom(wer, group);
				
				if (w2 != null && w != w2 && stayPriority(group, w2) > p) {
					STATS.WORK().EMPLOYED.set(h, w2);
					return;
				}
				
			}
			
			{
				Humanoid h2 = wer.get(group);
				if (h2 != null) {
					RoomInstance w2 = work(h2);
					if (w != w2) {
						
						double swap = stayPriority(group, w2) + stayPriority(group(h2), w);
						swap -=  stayPriority(group, w) + stayPriority(group(h2), w2);
						
						if (swap > 0) {
							h2.interrupt();
							
							
							
							
							
							STATS.WORK().EMPLOYED.set(h2, w);
							STATS.WORK().EMPLOYED.set(h, w2);
							wer.set(null, group);
							return;
						}
						
						
						
					}
				}
			}
			
			for (int i = 0; i < WGROUP.all().size(); i++) {
				WGROUP g = WGROUP.all().get(i);
				if (group == g)
					continue;
				Humanoid nn = wer.get(g);
				if (nn == null) {
					wer.set(h, g);
				}else {
					double p2 = stayPriority(group(nn),work(nn));
					if (p < p2) {
						wer.set(h, g);
					}else if(p == p2 && RND.oneIn(w.blueprintI().employment().employed())) {
						wer.set(h, g);
					}
				}					
			}
		}
		
		private RoomInstance updateBestRoom(W wer, WGROUP group) {
			
			if (wer.roomCounts[group.index] >= wer.emp.blueprint().instancesSize()) {
				wer.roomCounts[group.index] = 0;
			}
			RoomInstance nextBestWork = getEmployableRoom(wer.emp.blueprint(), wer.roomCounts[group.index]);
			wer.roomCounts[group.index]++;
			
			RoomInstance currentBestWork = getEmployableRoom(wer.emp.blueprint(), wer.currentRoom[group.index]);
			
			if (nextBestWork != null) {
				
				if (currentBestWork == null) {
					wer.currentRoom[group.index] = wer.roomCounts[group.index];
					currentBestWork = nextBestWork;
				}else {
					double cp = stayPriority(group, currentBestWork);
					double np = stayPriority(group, nextBestWork);
					if (np > cp || (np == cp && nextBestWork.employees().target()-nextBestWork.employees().employed() > currentBestWork.employees().target()-currentBestWork.employees().employed())) {
						wer.currentRoom[group.index] = wer.roomCounts[group.index];
						currentBestWork = nextBestWork;
					}
				}
			}
			
			return currentBestWork;
		}
		
		private RoomInstance getEmployableRoom(RoomBlueprintIns<?> current, int ri) {
			if (ri < current.instancesSize()) {
				RoomInstance ins = current.getInstance(ri);
				if (ins.employees().employed() < ins.employees().target()) {
					return ins;
				}
			}
			return null;
		}
		
		public double stayPriority(WGROUP group, RoomInstance work) {
			if (work == null)
				return 1.0;
			
			if (work.employees().preffered().is(group))
				return 1.0;
			return 0.5*group.race.pref().structure(BUILDING_PREFS.get(work.mX(), work.mY()));
		}
		
		
		private static class W {
			
			public final RoomEmploymentSimple emp;
			private Humanoid[] as = new Humanoid[WGROUP.all().size()];
			private int[] roomCounts = new int[WGROUP.all().size()];
			private int[] currentRoom = new int[WGROUP.all().size()];
			
			W(RoomEmploymentSimple emp){
				this.emp = emp;
			}
			
			public Humanoid get(WGROUP g) {
				Humanoid a = as[g.index];
				if (a != null) {
					if (!a.isRemoved() && group(a) != g) {
						RoomInstance ins = work(a);
						if (ins != null && ins.blueprintI().employment() == emp)
							return a;
					}
					as[g.index] = null;
				}
				return null;
			}
			
			private void set(Humanoid h, WGROUP g) {
				as[g.index] = h;
			}
			
		}
		
		private static WGROUP group(Humanoid h) {
			return WGROUP.get(h) == null ? WGROUP.get(HTYPES.SUBJECT() , h.race()): WGROUP.get(h);
		}
		
		
	}
	

}
