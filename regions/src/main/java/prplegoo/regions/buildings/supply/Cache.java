package prplegoo.regions.buildings.supply;

import init.resources.RESOURCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import world.region.RD;

import java.io.IOException;

final class Cache implements SAVABLE{
	Cache(ROOM_LOGISTICS b) {
		
	}
	
	public int needed(RESOURCE res) {
		return RD.DEFICITS().getOutstandingDeficit(res);
	}
	
	public int deliverableSecret(RESOURCE res) {
		return RD.DEFICITS().getOutstandingDeficit(res);
	}
	
	public int deliverable(RESOURCE res) {
		return deliverableSecret(res);
	}
	
	public int deliver(RESOURCE res, int am) {
		double needed = deliverable(res);
		if (needed <= 0) {
			return 0;
		}

		if (needed < am) {
			RD.DEFICITS().addSupplies(res, (int) needed);

			return (int) needed;
		} else {
			RD.DEFICITS().addSupplies(res, am);

			return am;
		}
	}

	@Override
	public void save(FilePutter file) {
		// NOP
	}

	@Override
	public void load(FileGetter file) throws IOException {
		// NOP
	}

	@Override
	public void clear() {
		// NOP
	}

}
