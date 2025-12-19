package prplegoo.regions.buildings.supply;

import game.GAME;
import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.logistics.MoveJob;
import settlement.room.infra.logistics.MoveJob.ROOM_MOVEJOBBER;
import settlement.room.infra.logistics.MoveJob.ROOM_MOVE_DEST;
import settlement.room.infra.logistics.MoveOrderPull;
import settlement.room.infra.logistics.MoveOrderPull.MoveOrderPullInstance;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import settlement.room.main.util.RoomInit;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;

import static settlement.main.SETT.ROOMS;

final class SupplyInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_RADIUS_INSTANCE, ROOM_MOVE_DEST, ROOM_MOVEJOBBER, MoveOrderPullInstance{

	private static final long serialVersionUID = 1L;
	public final static int ORDERS = 2;
	
	byte coolFetch = 0;
	final MoveOrderPull[] orders = new MoveOrderPull[ORDERS];
	boolean fetch = true;
	boolean auto = true;
	short[] tdata;
	private short ox,oy;
	private byte orderI = 0;
	private final RBITImp allowed = new RBITImp();
	final JobPositions<SupplyInstance> jobs;
	byte liveCount;
	byte goCount;
	private boolean prio = true;

	SupplyInstance(ROOM_LOGISTICS blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		int w = (int) (blueprint.constructor.workers.get(this));
		employees().maxSet(w*4);
		employees().neededSet(w);
		blueprint.tally.init(this);
		allowed.clear();
		activate();
	}


	
	void reset() {
		coolFetch = 0;
		for (MoveOrderPull o : orders) {
			if (o != null)
				o.cooldown = 0;
		}
		verifyCrates();
	}
	
	private void verifyCrates() {
		for (int i = 0; i < jobs.size(); i++) {
			int ox = jobs.get(i).x();
			int oy = jobs.get(i).y();
			Crate cr = blueprintI().crate.get(ox, oy);
			if (cr.storage() != null && cr.realResource() != null && !allowed.has(cr.storage().resource())) {
				cr.clear();
			}
		}
	}
	

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub

	}
	
	void allowedToggle(RESOURCE res) {
		for (MoveOrderPull p : orders) {
			if (p != null)
				p.resbits.set(res, !allowed.has(res));
		}
		allowed.toggle(res);
	}
	
	public RBIT allowed() {
		return allowed;
	}

	
	@Override
	protected void updateAction(double updateInterval, boolean day) {
		if (!active() || employees().employed() <= 0) {
			for (int i = 0; i < jobs.size(); i++) {
				int ox = jobs.get(i).x();
				int oy = jobs.get(i).y();
				Crate cr = blueprintI().crate.get(ox, oy);
				if (cr.storage() != null && cr.realResource() != null) {
					cr.clear();
				}
			}
		}
		jobs.searchAgain();
		if (coolFetch > 0) {
			coolFetch--;
		}
		for (MoveOrderPull o : orders) {
			if (o != null && o.cooldown > 0)
				o.cooldown--;
		}
	}
	
	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().crate.get(c.x(), c.y()) != null) {
				blueprintI().crate.dispose();
			}
		}
	}
	
	@Override
	public ROOM_LOGISTICS blueprintI() {
		return ROOMS().LOGISTICS;
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		return true;
	}

	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		return null;
	}

	@Override
	public MoveOrderPull[] moveOrdersPull() {
		return orders;
	}

	@Override
	public RBIT moveCapacity() {
		return allowed;
	}

	@Override
	public RBIT moveOrderPullAvailable() {
		return blueprintI().tally.fetchBit(this, allowed);
	}
	
	@Override
	public RBIT moveOrderPullAccepted() {
		return moveCapacity();
	}

	@Override
	public int moveMinAmount() {
		return 1;
	}

	@Override
	public int moveMaxRadius() {
		return 300;
	}
	
	public void prioritizeToggle() {
		prio = !prio;
		reset();
	}

	public boolean prioritizing() {
		return prio;
	}
	
	public void fetchingToggle() {
		fetch = !fetch;
		reset();
	}

	public boolean fetching() {
		return fetch;
	}

	@Override
	public MoveJob moveJob(Humanoid skill) {
		
		RBIT bb = blueprintI().tally.fetchBit(this, allowed);
		if (bb.isClear())
			return null;
		
		int am = SETT.ROOMS().STOCKPILE.carryCap(skill);
		
		if ((fetch || prio) && coolFetch <= 0){

			MoveJob j = MoveJob.fetch(this, this, am, radius(), ox, oy, fetch ? bb : RBIT.NONE, prio ? bb : RBIT.NONE);
			
			if (j != null) {
				ox = (short) j.source.x();
				oy = (short) j.source.y();
				return j;
			}
			
			coolFetch = 4;
		}
		
		for (int ooi = 0; ooi < orders.length; ooi++) {
			orderI++;
			if (orderI >= orders.length)
				orderI = 0;
			MoveOrderPull p = orders[orderI];
			if (p != null && p.cooldown <= 0) {
				MoveJob j = p.job(this, Math.min(am, 1), am);
				
				if (j != null) {
					p.cooldown = -1;
					return j;
				}
				p.cooldown = 4;
			}
		}
		
		
		return null;
	}

	@Override
	public TILE_STORAGE destCrate(RBIT okMask, int minAm, int ox, int oy) {
		
		RBIT bb = blueprintI().tally.fetchBit(this, allowed);
		if (bb.isClear())
			return null;
		
		if (is(ox, oy)){
			Crate cr = blueprintI().crate.get(ox, oy);
			TILE_STORAGE s = cr.get(ox, oy).storage();
			if (s != null && cr.realResource() != null && okMask.has(s.resource()) && s.storageReservable() >= minAm) {
				return s;
			}
			
			
		}
		
		int eX = -1;
		int eY = -1;
		
		for (int i = 0; i < jobs.size(); i++) {
			ox = jobs.get(i).x();
			oy = jobs.get(i).y();
			Crate cr = blueprintI().crate.get(ox, oy);
			TILE_STORAGE s = cr.get(ox, oy).storage();
			if (s != null) {
				if (cr.realResource() == null) {
					if (eX == -1) {
						eX = ox;
						eY = oy;
					}
					
				}else if (okMask.has(s.resource()) && s.storageReservable() >= minAm){
					return s;
				}
			}
			
		}
		
		if (eX == -1) {
			LOG.ln("Weird " + mX() + " " + mY());
			okMask.debug();
			reset();
			return null;
		}
		
		Crate cr = blueprintI().crate.get(eX, eY);
		cr.clear();
		cr.resourceSet(blueprintI().tally.getNewCrate(GAME.updateI(), okMask));
		cr = blueprintI().crate.get(eX, eY);
		return cr.storage();
	}

	@Override
	public RBIT destSpaceMask() {
		return moveOrderPullAvailable();
	}

	@Override
	public double storedD(RESOURCE res) {
		return 0;
	}
	

	
	@Override
	public int radius() {
		return 200;
	}
	
	@Override
	public byte radiusRaw() {
		return 0;
	}

	@Override
	public void radiusRawSet(byte r) {
		
	}

	@Override
	public boolean searching() {
		if (employees().employed() > 0) {
			if (coolFetch <= 0)
				return true;
			for (MoveOrderPull p : orders) {
				if (p != null && p.cooldown <= 0)
					return true;
			}
		}
		return false;
	}


	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return blueprintI().crate.storage(tx, ty);
	}


	private static class Jobs extends JobPositions<SupplyInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(SupplyInstance ins) {
			super(ins);
		}
		
		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().crate.get(tx, ty) != null;
		}
		
		@Override
		protected SETT_JOB get(int tx, int ty) {
			Crate c = ins.blueprintI().crate.get(tx, ty);
			if (c != null)
				return c.job;
			return null;
		}
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}



	@Override
	public void copyFrom(MoveOrderPullInstance same) {
		SupplyInstance ins = (SupplyInstance) same;
		for (RESOURCE res : RESOURCES.ALL()) {
			if (allowed().has(res) != ins.allowed().has(res)) {
				allowedToggle(res);
				reset();
			}
		}		
		fetch = ins.fetch;
		coolFetch = 0;
		auto = ins.auto;
		employees().neededSet(ins.employees().target());
	}

}
