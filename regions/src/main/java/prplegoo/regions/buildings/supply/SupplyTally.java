package prplegoo.regions.buildings.supply;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.room.infra.logistics.MoveDic;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.ArrayListGrower;
import util.dic.Dic;

import java.util.Arrays;

public final class SupplyTally{
	

	final ArrayListGrower<TallyData> datas = new ArrayListGrower<>();
	private int unusedCrate = 0;
	public final TallyData crates = new TallyData(MoveDic.¤¤crates);
	public final TallyData spaceReserved = new TallyData(Dic.¤¤Inbound);
	public final TallyData amount = new TallyData(MoveDic.¤¤Stored);
	private final ROOM_LOGISTICS b;


	

	
	public SupplyTally(ROOM_LOGISTICS b) {
		this.b = b;
	}

	void clear() {
		for (TallyData d : datas)
			d.clear();
	}
	
	void init(SupplyInstance ins) {
		ins.tdata = new short[datas.size()*(RESOURCES.ALL().size()+1)+1];
		for (int ji = 0; ji < ins.jobs.size(); ji++) {
			COORDINATE c = ins.jobs.get(ji);
			if (ins.is(c)) {
				Crate cr = b.crate.get(c.x(), c.y());
				if (cr != null)
					report(cr, ins, 1);
			}
		}
		ins.reset();
	}
	
	void report(Crate crate, SupplyInstance ins, int delta) {
		
		
		
		if (crate.storage() != null) {
			RESOURCE a = crate.realResource();
			if (a != null) {
				crates.inc(ins, a, delta);
				spaceReserved.inc(ins, a, delta*crate.storage().storageReserved());
				amount.inc(ins, a, delta*crate.resAmount());
			}else {
				ins.tdata[datas.size()*(RESOURCES.ALL().size()+1)] += delta;
				unusedCrate += delta;
			}	
		}
	}




	public int amountTotal(RESOURCE res) {
		return amount.total(res);
	}

	public int unusedCrates() {
		return unusedCrate;
	}

	public int unusedCrates(SupplyInstance ins) {
		return ins.tdata[datas.size()*(RESOURCES.ALL().size()+1)];
	}
	
	public class TallyData {
		
		private final int insStride;
		private final int[] ams = new int[RESOURCES.ALL().size()+1];
		public final CharSequence name;
		private final int index;
		
		TallyData(CharSequence name){
			this.name = name;
			index = datas.add(this);
			insStride = index*(RESOURCES.ALL().size()+1);
		}
		
		public int total(RESOURCE a) {
			if (a == null)
				return ams[RESOURCES.ALL().size()];
			return ams[a.index()];
		}
		
		public int get(SupplyInstance ins, RESOURCE a) {
			if (a == null)
				return ins.tdata[insStride + RESOURCES.ALL().size()];
			return ins.tdata[insStride + a.index()];
		}
		
		private void inc(SupplyInstance ins, RESOURCE a, int am) {
			set(ins, a, get(ins, a)+am);
		}
		
		private void set(SupplyInstance ins, RESOURCE a, int am) {
			int old = ins.tdata[insStride + a.index()];
			ams[a.index()] -= old;
			ams[RESOURCES.ALL().size()] -= old;
			ins.tdata[insStride + a.index()] = (short) am;
			ins.tdata[insStride + RESOURCES.ALL().size()] = (short) am;
			ams[a.index()] += am;
			ams[RESOURCES.ALL().size()] -= am;
			
			if (ams[a.index()] < 0)
				throw new RuntimeException("" + a + " " + name);
		}
		
		private void clear() {
			Arrays.fill(ams, 0);
		}
		
	}
	
	RESOURCE getNewCrate(int ai, RBIT allowed) {
		
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			ai %= RESOURCES.ALL().size();
			RESOURCE res = RESOURCES.ALL().get(ai);
			if (allowed.has(res)) {
				int am = b.cache.needed(res); 
		
			
				if (crates.total(res)* ROOM_LOGISTICS.STORAGE < am)
					return res;
			}
			ai++;
		}
		
		
		return null;
		
	}
	
	int fetchAmount(RESOURCE a) {
		
		if (a == null)
			return 0;
		
		int am = b.cache.needed(a);
		am -= spaceReserved.total(a);
		am -= amount.total(a);
		return am;
		
	}
	
	private final RBITImp tmp = new RBITImp();
	
	RBIT fetchBit(SupplyInstance ins, RBIT allowed) {
		
		tmp.clear();
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (!allowed.has(res))
				continue;
			
			if (capacity(ins, res) > 0) {
				int am = b.cache.needed(res);
				am -= spaceReserved.total(res);
				am -= amount.total(res);
				if (am > 0) {
					tmp.or(res);
				}
			}else if (otherCapacity(ins, res) > 0) {
				int am = b.cache.needed(res);
				am -= crates.total(res)* ROOM_LOGISTICS.STORAGE;
				if (am > 0) {
					tmp.or(res);
				}
			}
		}
		
		return tmp;
		
	}
	
	private int capacity(SupplyInstance ins, RESOURCE res) {
		return crates.get(ins, res)* ROOM_LOGISTICS.STORAGE - spaceReserved.get(ins, res) - amount.get(ins, res);
	}
	
	int otherCapacity(SupplyInstance ins, RESOURCE res) {
		return unusedCrates(ins)* ROOM_LOGISTICS.STORAGE;
	}
	
	int capacity(SupplyInstance ins, RESOURCE res, RBIT allowed) {
		if (!allowed.has(res))
			return 0;
		return capacity(ins, res) + otherCapacity(ins, res);
		
	}
	
}
