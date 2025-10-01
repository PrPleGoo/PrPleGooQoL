package settlement.entity.humanoid.ai.work;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import prplegoo.regions.buildings.supply.ROOM_LOGISTICS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.work.WorkAbs.Works;
import settlement.main.SETT;
import settlement.stats.STATS;

import static settlement.main.SETT.PATH;

final class PrPleGooWorkTransporterLogistics extends PlanBlueprint {

	private final WorkDeliveryman deliveryman;
	private final WorkAbs work;
	
	protected PrPleGooWorkTransporterLogistics(AIModule_Work module, PlanBlueprint[] map, Works w) {
		super(module, SETT.ROOMS().LOGISTICS, map);
		map[b().index()] = null;
		deliveryman = new WorkDeliveryman(module, map, blueprint, false);
		map[b().index()] = null;
		work = new WorkAbs("WorkTransportLogisticsExtra", module, blueprint, map, w);
		map[b().index()] = this;
	}
	
	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {
		
		AiPlanActivation p = super.activate(a, d);
		if (p != null)
			return p;
		
		p = work.activate(a, d);
		
		if (p != null) {
			return p;
		}
		
		return deliveryman.activate(a, d);
	}
	

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {

		int am = b().goReserve(work(a).mX(), work(a).mY(), d.planTile);
		if (am == 0)
			return null;
		d.planByte1 = (byte) am;
		return start.set(a, d);
		
	}
	
	@Override
	public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) <= 0.7;
	}
	
	
	private static ROOM_LOGISTICS b() {
		return SETT.ROOMS().LOGISTICS;
	}

	
	private final Resumer start = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.planTile);
			if (s == null) {
				can(a, d);
				return null;
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			RESOURCE res = b().goCrate(d.planTile, d.planByte1);
			if (res == null) {
				can(a, d);
				return null;
			}
			d.planByte2 = res.bIndex();
		
			return walk.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b().goCancel(d.planTile, d.planByte1);
		}
	};
	
	private final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				AISubActivation s =AI.SUBS().walkTo.pathFull(a, d);
				if (s != null) {
					int dx = d.planTile.x();
					int dy = d.planTile.y();
					int ran = SETT.tileRan(dx, dy);
					SETT.HALFENTS().transports.make(a, dx, dy, RESOURCES.ALL().get(d.planByte2), (byte) ran, true);
					return s;
				}
			}
			can(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			can(a, d);
			return stayAway.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b().goCancel(d.planTile, d.planByte1);
		}
	};
	
	private final Resumer stayAway = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			SETT.ENTITIES().moveIntoTheTheUnknown(a);
			a.speed.magnitudeInit(0);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.9) {
				can(a, d);
				return null;
			}
			if (AIModules.nextPrio(d) > 7) {
				can(a, d);
				return null;
			}
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT.ENTITIES().returnFromTheTheUnknown(a);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
	};
	
	@Override
	protected double transportAmount(Humanoid a, AIManager d) {
		if (getResumer(d) == walk) {
			return (double)d.planByte1 / ROOM_LOGISTICS.STORAGE;
		}
		return -1;
	}

}
